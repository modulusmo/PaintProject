package paint;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javafx.scene.AccessibleAttribute;
import javax.imageio.ImageIO;
import javax.management.Query;
public class SaveAsJPG {

	/**
	 * @param args the command line arguments
	 */
    public static final int BLOCKSIZE = 8;
    
    private static final double[][] quantMatrixLuma = {
        { 16, 11, 10, 16, 24, 40, 51, 61},
        { 12, 12, 14, 19, 26, 58, 60, 55},
        { 14, 13, 16, 24, 40, 57, 69, 56},
        { 14, 17, 22, 29, 51, 87, 80, 62},
        { 18, 22, 37, 56, 68,109,103, 77},
        { 24, 35, 55, 64, 81,104,113, 92},
        { 49, 64, 78, 87,103,121,120,101},
        { 72, 92, 95, 98,112,100,103, 99}
    };
    private static final double[][] quantMatrixChroma = {
        { 17, 18, 24, 47, 99, 99, 99, 99},
        { 18, 21, 26, 66, 99, 99, 99, 99},
        { 24, 26, 56, 99, 99, 99, 99, 99},
        { 47, 66, 99, 99, 99, 99, 99, 99},
        { 99, 99, 99, 99, 99, 99, 99, 99},
        { 99, 99, 99, 99, 99, 99, 99, 99},
        { 99, 99, 99, 99, 99, 99, 99, 99},
        { 99, 99, 99, 99, 99, 99, 99, 99}
    };
    private static final double quality = 70;//1 is poor quality, 100 is high quality
    
    static class Number{
        public byte value;
        public int occurance;
        public short code;

        public Number() {
            occurance = 0;
            value=0;
        }
    }
    
    static class RLEpair{
        public int skip;
        public byte value;
        
        public RLEpair(){
            skip=0;
            value=0;
        }
        public RLEpair(int s, int v){
            skip=s;
            value=value;
        }
    }
    
    //public static void main(String[] args) {
    public SaveAsJPG(BufferedImage image) {
        
        //BufferedImage image = null;
        
        try {
            //File pnginput = new File ("C:\\Users\\Moses Cuevas\\Pictures\\parts.png");
            //image = ImageIO.read(pnginput);

            int i,j;
            int oneDimCounter=0;

            //get information
            int height= image.getHeight();
            int width = image.getWidth();

            int newWidth = width + (width % BLOCKSIZE);//make picture divisible by 8
            int newHeight= height + (height % BLOCKSIZE);
            //Color [][] colors = new Color [width][height];
            double [][] luma = new double [newWidth][newHeight];
            double [][]dctInput= new double [BLOCKSIZE][BLOCKSIZE];
            double [][]dctResult=new double [BLOCKSIZE][BLOCKSIZE];//after the dct is calculated
            double [][]dctQuantized=new double[BLOCKSIZE][BLOCKSIZE];
            
            Number[] huffTable;
            huffTable = new Number[256];
            
            //initialize table
            byte tempNum = -128;
            for(int k=0; k<256; k++){
                huffTable[k] = new Number();
                huffTable[k].value = tempNum;
                tempNum = (byte)(tempNum + 1);
            }

            
            
            int []oneDimArray = new int [newWidth * newHeight];
            byte[]outputbytes=new byte[newWidth*newHeight+3];//needs work
            double red,green,blue;

            //change to YCbCr
            for(i=0; i<width; i++){
                for(j=0; j<height; j++){
                    Color mycolor = new Color(image.getRGB(i, j));
                    red = mycolor.getRed();
                    green = mycolor.getGreen();
                    blue = mycolor.getBlue();

                    luma[i][j] = (double)((.299 * red) + (.578 * green) + (.114 * blue) -127);//<-- -127 to produce values around zero
                }
            }
            
            //fill the rest with zero
            for(i = width; i < newWidth ;i++){
                for(j = 0; j < newHeight;j++){
                    luma[i][j] = 0;
                }
            }
            for(i = 0; i < width ;i++){
                for(j = height; j < newHeight;j++){
                    luma[i][j] = 0;
                }
            }
            
            /*for(i=0; i<newWidth; i++){
                for(j=0; j<newHeight; j++){
                    System.out.print(luma[i][j]+"\t");
                }
                System.out.print("\n");
            }*/
            
            //divide into 8x8 blocks
            int xstart,ystart;
            int a,b;

            for (i = 0; i < newWidth / BLOCKSIZE; i++) { 
                for (j = 0; j < newHeight / BLOCKSIZE; j++) {

                    xstart = i * BLOCKSIZE;
                    ystart = j * BLOCKSIZE;
                    // dividing into 8x8 blocks
                    for (a = 0; a < BLOCKSIZE; a++) {
                        for (b = 0; b < BLOCKSIZE; b++) {
                            dctInput[a][b] = luma[xstart + a][ystart + b];
                        }
                    }

                    // Apply DCT to block
                    dctResult = calcDCT(dctInput);
                    
                    // quantize the block
                    dctQuantized = quantize(dctResult);

                    //huffman encode - convert to 1D array and count frequency
                    /*byte tempByte;
                    for (a = 0; a < BLOCKSIZE; a++) {
                        for (b = 0; b < BLOCKSIZE; b++) {
                            tempByte = (byte)dctQuantized[a][b];
                            oneDimArray[oneDimCounter] = tempByte;
                            huffTable[(tempByte+128)].occurance++;//add one to the occurance
                            oneDimCounter++;
                        }
                    }*/
                    
                    //now that we know what values are
                    
                    //zigzag with runlength encoding
                    oneDimCounter=0;
                    int zeroCounter=0;
                    boolean upward = true;
                    int w=0,h=0;
                    byte tempRLEvalue;
                    RLEpair[] rlePairs = new RLEpair[BLOCKSIZE*BLOCKSIZE]; 
                    int rlePairCounter=0;
                    while(oneDimCounter != BLOCKSIZE*BLOCKSIZE){
                        
                        //oneDimArray[oneDimCounter++] = (byte)dctQuantized[w][h];
                        oneDimCounter++;
                        tempRLEvalue = (byte)dctQuantized[w][h];
                        
                        //runlength encode
                        //if(oneDimArray[oneDimCounter] == 0){
                        if(tempRLEvalue == 0){
                            zeroCounter++;
                        }
                        else{
                            //rlePairs[rleCounter++] = new RLEpair(zeroCounter,oneDimArray[oneDimCounter]);
                            rlePairs[rlePairCounter++] = new RLEpair(zeroCounter,tempRLEvalue);
                            huffTable[(tempRLEvalue+128)].occurance++;//add one to the occurance
                            zeroCounter=0;
                        }
                        
                        if(upward){//zigzag logic
                            if(h==0 && w != BLOCKSIZE-1){
                                upward = false;
                                w++;
                            }
                            else if(w == BLOCKSIZE-1){
                                upward=false;
                                h++;
                            }
                            else{
                                h--;
                                w++;
                            }
                        }
                        else{//downward
                            if(w==0 && h != BLOCKSIZE-1){
                                upward = true;
                                h++;
                            }
                            else if (h == BLOCKSIZE-1){
                                upward=true;
                                w++;
                            }
                            else{
                                w--;
                                h++;
                            }
                        }
                    }
                    rlePairs[rlePairCounter] = new RLEpair();//last rle has(0,0)
                    
                    //run length encode
                    
                    
                }
            }
            
            //for logic checking purposes
            for (i = 0; i < newWidth*newHeight; i++) { 
                System.out.print(oneDimArray[i]+"\t");
                if((i+1)% newWidth == 0)
                    System.out.print("\n");
            }
            
            for(int k=0;k<huffTable.length;k++){
                System.out.println(huffTable[k].occurance + " " + huffTable[k].value);
            }
            System.out.println("\nStarting sorting");
            
            //sort by highest occurance to lowest
            int k,m;
            Number key = new Number();
            for(k=0; k<256; k++){
                for(m=0;m<256-k-1;m++){
                    if(huffTable[m].occurance<huffTable[m+1].occurance
                        || (huffTable[m].occurance==huffTable[m+1].occurance 
                            && huffTable[m].value<huffTable[m+1].value)){
                        
                        key.occurance = huffTable[m+1].occurance;
                        key.value = huffTable[m+1].value;
                        
                        huffTable[m+1].occurance = huffTable[m].occurance;
                        huffTable[m+1].value = huffTable[m].value;
                        
                        huffTable[m].occurance = key.occurance;
                        huffTable[m].value = key.value;
                    }
                }
            }
            
            System.out.println("Huff occurance\n");
            for(k=0;k<huffTable.length;k++){
                System.out.println(huffTable[k].occurance + " " + huffTable[k].value);
            }
            

            //write the image to a file, resource: http://web.cecs.pdx.edu/~harry/compilers/ASCIIChart.pdf
            //http://www.file-recovery.com/jpg-signature-format.htm
            outputbytes[0]= -1;  //ff StartOfImage
            outputbytes[1]= -40; //d8
            
            outputbytes[2]= -1;  //ff Application Marker
            outputbytes[3]= -32; //e0
            
            outputbytes[4]= 0;   //00 Length must be >=16
            outputbytes[5]= 16;  //10
            
            outputbytes[6]= 74;//4a 'j' conforms to jfif
            outputbytes[7]= 70;//46 'f'
            outputbytes[8]= 73;//49 'i'
            outputbytes[9]= 70;//46 'f'
            outputbytes[10]= 0;//00
            
            outputbytes[11]= 1;   // 01 major revision num 
            outputbytes[12]= 1;   // 01 minor revision num
            outputbytes[13]= 1;   // 01 units for xy density
            outputbytes[14]= 0;   // X density
            outputbytes[15]= 0x60;// 
            outputbytes[16]= 0;   // Y density
            outputbytes[17]= 0x60;//
            outputbytes[18]= 0;   // Thumbnail width
            outputbytes[19]= 0;   // Thumbnail height
            
            //thumbnail ignored
            
            //---------------------------------------------------------------DQT (Y)
            outputbytes[20] = (byte)0xff;//Quantization Table for luma
            outputbytes[21] = (byte)0xdb;
            outputbytes[22] = (byte)0x00;//length
            outputbytes[23] = (byte)0x40;
            outputbytes[24] = (byte)0x00;//qt info number|size
            int tempCounter=25;
            for(k=0;k<BLOCKSIZE;k++){
                for(m=0;m<BLOCKSIZE;m++){
                    outputbytes[tempCounter++] = (byte) quantMatrixLuma[k][m];
                }
            }//by the end, tempcounter = 89
            
            //---------------------------------------------------------------SOF
            outputbytes[90] = (byte)0xff;//start of frame
            outputbytes[91] = (byte)0xc0;
            
            outputbytes[92] = (byte)0x00;//length      <---      <---      <---      <---      <---     <---     <---
            outputbytes[93] = (byte)0xff;
            
            outputbytes[94] = (byte)0x08;//data precision
            
            outputbytes[95] = (byte)(height>>16);//image height
            outputbytes[96] = (byte)(height>>24);
           
            outputbytes[97] = (byte)(width>>16);//image width
            outputbytes[98] = (byte)(width>>24);
            
            outputbytes[99] = (byte)0x01;//number of components
            
            //for each component
            outputbytes[100] = (byte)0x01;//component id
            outputbytes[101] = (byte)0x88;//sampling factors vert|horiz
            outputbytes[102] = (byte)0x00;//quantTable number
            
            
            //---------------------------------------------------------------DHT
            outputbytes[103] = (byte)0xff;//huffman Table
            outputbytes[104] = (byte)0xc4;
            
            outputbytes[105] = (byte) 0x00;//length
            outputbytes[105] = (byte) 0x00;
            
            outputbytes[105] = (byte) 0x00;//info number|type (DC)
            
            //number of symbols per type (16 bytes total)
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            
            //---------------------------------------------------------------SOS
            outputbytes[105] = (byte) 0xff;//Start of Scan
            outputbytes[105] = (byte) 0xda;
            
            outputbytes[105] = (byte) 0x00;//length
            outputbytes[105] = (byte) 0x00;//6+2*(number of components in scan)
            
            outputbytes[105] = (byte) 0x00;//number of components in scan
            
            //for each component
            outputbytes[105] = (byte) 0x01;//component id
            outputbytes[105] = (byte) 0x00;//DHT to use AC|DC
            
            outputbytes[105] = (byte) 0x00;//must skip 3 bytes
            outputbytes[105] = (byte) 0x00;
            outputbytes[105] = (byte) 0x00;
            
            //image data start
            
            
            
            //---------------------------------------------------------------EOI
            outputbytes[200]= -1;//ff endOfImage
            outputbytes[201]= -39;//d9
            
            try( //ImageIO.write(image, "png",new File("C:\\Users\\Moses Cuevas\\Pictures\\newBird.jpg"));
                FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir")+"\\savedOutputFile.jpg")) {
                stream.write(outputbytes);
            }
            //ImageIO.write(image, "jpg",new File("C:\\Users\\Moses Cuevas\\Pictures\\cheat.jpg"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }
    
    // -------------  -------------  -------------  -------------  -------------  ------------- functions 

    private static double[][] calcDCT(double [][] dctarray){

        double[][] buffer = new double[BLOCKSIZE][BLOCKSIZE];

        for (int u = 0; u < BLOCKSIZE; u++){
            for (int v = 0; v < BLOCKSIZE; v++){
                
                buffer[u][v] = (double)(.25) * coefficient((int) u) * coefficient((int) v);//may need to cast to double
                //System.out.print(buffer[u][v]+"\t");
                double xysum = 0;
                for (int x = 0; x < BLOCKSIZE; x++){
                    for (int y = 0; y < BLOCKSIZE; y++){
                        //System.out.print(dctarray[x][y]+"\t");
                        xysum += dctarray[y][x]//flip for transpose
                                      * Math.cos(((2 * x + 1) * u * Math.PI) / (2.0*BLOCKSIZE))
                                      * Math.cos(((2 * y + 1) * v * Math.PI) / (2.0*BLOCKSIZE));
                    }
                   // System.out.print("\n");
                }
                buffer[u][v] *= xysum;
            }
            //System.out.print("\n");
        }
        //System.out.print("\n");
        return (buffer);
    }
    
    private static double coefficient(int index){// <-- <-- <-- don't forget to rename this!!! <-- <-- <--
        double result;
        if(index == 0){
            result = (double) (1.0/Math.sqrt(2.0));
        }
        else{
            result = (double) 1.0;
        }
        return result;
    }
    
    private static double[][] quantize(double [][] dctarray){
        int i, j;
        double[][] quantum = new double[BLOCKSIZE][BLOCKSIZE];
        for (i = 0; i < BLOCKSIZE; i++){
            for (j = 0; j < BLOCKSIZE; j++){
            	//quantum[i][j] = dctarray[i][j]/(1 + ((1 + i + j) * quality));
                quantum[i][j] = dctarray[i][j]/Math.floor((S(quality)*quantMatrixLuma[i][j]+50)/100);
            }
        }
        return quantum;
    }
    
    private static double S(double q){
        if(q<50){
            return 5000/q;
        }
        else{
            return 200 - 2 * q;
        }
    }
}
