package saveasjpg;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
public class SaveAsJPG {

	/**
	 * @param args the command line arguments
	 */
    public static final int BLOCKSIZE = 8;
    
	public static void main(String[] args) {
		BufferedImage image = null;
        try {
            File pnginput = new File ("C:\\Users\\Moses Cuevas\\Pictures\\knowswhatsgood.png");
            image = ImageIO.read(pnginput);
            
            int i,j;
            
            //get information
            int height= image.getHeight();
            int width = image.getWidth();
            
            int newWidth = width + (width % BLOCKSIZE);//make picture divisible by 8
            int newHeight= height + (height % BLOCKSIZE);
            //Color [][] colors = new Color [width][height];
            int [][] lumina = new int [newWidth][newHeight];
            int [][]DCTinput= new int [BLOCKSIZE][BLOCKSIZE];
            int [][]DCTresult=new int [newWidth/BLOCKSIZE][newHeight/BLOCKSIZE];//after the dct is calculated
            float red,green,blue;
            
            //change to YCbCr
            for(i=0; i<width; i++){
                for(j=0; j<height; j++){
                    Color mycolor = new Color(image.getRGB(i, j));
                    red = mycolor.getRed();
                    green = mycolor.getGreen();
                    blue = mycolor.getBlue();
                    
                    lumina[i][j] = (int)((.299 * red) + (.578 * green) + (.114 * blue));
                }
            }
            //fill the rest with zero
            for(i = width; i < newWidth ;i++){
                for(j = height; j<newHeight;j++){
                    lumina[i][j] = 0;
                }
            }
            
            //divide into 8x8 blocks
            for(i=0; i<i+BLOCKSIZE && i<newWidth; i=i+BLOCKSIZE){
                for(j=0; j<j+BLOCKSIZE && j<newHeight; j=j+BLOCKSIZE){
                    DCTinput[i%BLOCKSIZE][j%BLOCKSIZE] = lumina[i][j];
                }
                DCTresult[i%BLOCKSIZE][j%BLOCKSIZE] = calcDCT(DCTinput);
            }
            
            //apply DCT to each block
            
            //quantize each block
            
            //huffman encode
            
            //write the image to a file
            
            //ImageIO.write(image, "png",new File("C:\\Users\\Moses Cuevas\\Pictures\\newBird.jpg"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
	}
	
        private int calcDCT(int [][] dctarray){//garbage code that works like garbage  <----  <-----  <-----  <-----
            int dctValue;
            
            for(int i=0;i<BLOCKSIZE;i++){
                for(int j=0;j<BLOCKSIZE;j++){
                    dctValue += value[j][i]
                                    * Math.cos(((2 * i + 1) * u * Math.PI) / 16)
                                    * Math.cos(((2 * j + 1) * v * Math.PI) / 16);
                }
            }
            
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) {
                    temp = 0.0;
                    for (x = 0; x < N; x++) {
                        for (y = 0; y < N; y++) {
                            temp += Cosines[x][i] *
                                Cosines[y][j] *
                                Pixel[x][y];
                        }
                    }
                    temp *= sqrt(2 * N) * Coefficient[i][j];
                    DCT[i][j] = INT_ROUND(temp);
                }
            }
            
            return dctValue;
        }
}
