/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package saveaspng;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import javax.imageio.ImageIO;
public class SaveasPNG {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        BufferedImage image = null;
 File pngimage = new File ("D:\\Pictures\\test.png");
image = ImageIO.read(pngimage);
int width          = image.getWidth();
int height         = image.getHeight();
int[][] result = PngGetPixels(image);
        try (OutputStream out = new FileOutputStream("PngOutput.png")) {
			write(result, out);
		}
	
    
}


private static int[][] PngGetPixels (BufferedImage image) {
final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
      final int width = image.getWidth();
      final int height = image.getHeight();
      final boolean hasAlphaChannel = image.getAlphaRaster() != null;

      int[][] result = new int[height][width];
      if (hasAlphaChannel) {
         final int pixelLength = 4;
         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
            argb += ((int) pixels[pixel + 1] & 0xff); // blue
            argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
               col = 0;
               row++;
            }
         }
      } else {
         final int pixelLength = 3;
         for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
            int argb = 0;
            argb += -16777216; // 255 alpha
            argb += ((int) pixels[pixel] & 0xff); // blue
            argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
            argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
            result[row][col] = argb;
            col++;
            if (col == width) {
               col = 0;
               row++;
            }
         }
      }

      return result; }
public static void write(int[][] result, OutputStream out) throws IOException {
		// PNG header (a pretty clever magic string)
		out.write(new byte[]{(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
		
		// IHDR chunk (image dimensions, color depth, compression method, etc.)
		int width = result[0].length;
		int height = result.length;
		byte[] ihdr = new byte[13];
		ihdr[ 0] = (byte)(width >>> 24);  // Big-endian
		ihdr[ 1] = (byte)(width >>> 16);
		ihdr[ 2] = (byte)(width >>>  8);
		ihdr[ 3] = (byte)(width >>>  0);
		ihdr[ 4] = (byte)(height >>> 24);  // Big-endian
		ihdr[ 5] = (byte)(height >>> 16);
		ihdr[ 6] = (byte)(height >>>  8);
		ihdr[ 7] = (byte)(height >>>  0);
		ihdr[ 8] = 8;  // Bit depth: 8 bits per sample
		ihdr[ 9] = 2;  // Color type: True color RGB
		ihdr[10] = 0;  // Compression method: DEFLATE
		ihdr[11] = 0;  // Filter method: Adaptive
		ihdr[12] = 0;  // Interlace method: None
		writeChunk("IHDR", ihdr, out);
		
		// IDAT chunk (pixel values and row filters)
		// Note: One additional byte at the beginning of each row specifies the filtering method
		if ((Integer.MAX_VALUE / height - 1) / width < 3)
			throw new IllegalArgumentException("Dimensions too large");
		int rowSize = width * 3 + 1;
		byte[] idat = new byte[rowSize * height];
		for (int y = 0; y < height; y++) {
			idat[y * rowSize + 0] = 0;  // Filter type: None
			for (int x = 0; x < width; x++) {
				int color = result[y][x];
				int index = y * rowSize + 1 + x * 3;
				idat[index + 0] = (byte)(color >>> 16);  // Red
				idat[index + 1] = (byte)(color >>>  8);  // Green
				idat[index + 2] = (byte)(color >>>  0);  // Blue
			}
		}
		idat = deflate(idat);
		writeChunk("IDAT", idat, out);
		
		// IEND chunk (no payload)
		writeChunk("IEND", new byte[0], out);
}

private static byte[] deflate(byte[] data) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		
		// zlib header
		b.write(0x08);  // Compression method: DEFLATE; window size: 256 bytes
		b.write(0x1D);  // Flag checksum, no preset dictionary, fastest compression level
		
		// DEFLATE data
		int offset = 0;
		do {
			int curBlockSize = Math.min(data.length - offset, 0xFFFF);
			int blockType = 0;  // BTYPE: No compression (verbatim)
			if (offset + curBlockSize == data.length)
				blockType |= 1;  // BFINAL
			b.write(blockType);
			b.write(curBlockSize >>> 0);  // Little-endian
			b.write(curBlockSize >>> 8);
			b.write((~curBlockSize) >>> 0);  // Ones' complement, little-endian
			b.write((~curBlockSize) >>> 8);
			b.write(data, offset, curBlockSize);
			offset += curBlockSize;
		} while (offset < data.length);
		
		// Final Adler-32 checksum
		Adler32 c = new Adler32();
		c.update(data);
		writeInt32((int)c.getValue(), b);
		
		return b.toByteArray();
	}
	
	
	// Writes the given chunk (with type name and payload data) to the given output stream.
	// This takes care of also writing the length and CRC.
	private static void writeChunk(String type, byte[] data, OutputStream out) throws IOException {
		CRC32 c = new CRC32();
		c.update(type.getBytes(StandardCharsets.US_ASCII));
		c.update(data);
		
		writeInt32(data.length, out);  // Length
		out.write(type.getBytes(StandardCharsets.US_ASCII));  // Type
		out.write(data);  // Data
		writeInt32((int)c.getValue(), out);  // CRC-32
	}
	
	
	// Writes the given 32-bit integer to the given output stream as bytes in big-endian.
	private static void writeInt32(int x, OutputStream out) throws IOException {
		out.write(x >>> 24);
		out.write(x >>> 16);
		out.write(x >>>  8);
		out.write(x >>>  0);
	}
	
}
	
	



