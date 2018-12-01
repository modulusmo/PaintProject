package saveasjpg;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
public class SaveAsJPG {

	/**
	 * @param args the command line arguments
	 */
    public static final int BLOCKSIZE = 8;
    
   private static final double[][] quantMatrix = {
        { 16, 11, 10, 16, 24, 40, 51, 61},
        { 12, 12, 14, 19, 26, 58, 60, 55},
        { 14, 13, 16, 24, 40, 57, 69, 56},
        { 14, 17, 22, 29, 51, 87, 80, 62},
        { 18, 22, 37, 56, 68,109,103, 77},
        { 24, 35, 55, 64, 81,104,113, 92},
        { 49, 64, 78, 87,103,121,120,101},
        { 72, 92, 95, 98,112,100,103, 99}
    };
    private static final double quality = 70;//1 is poor quality, 100 is high quality
    
    public class Number{
        public byte value;
        public byte occurance;

        public Number() {
            occurance = 0;
        }
        
        
    }
    
    public class Sortbyoccurance{
        
    }
    
    public static void main(String[] args) {
        
        BufferedImage image = null;
        
        try {
            File pnginput = new File ("C:\\Users\\Moses Cuevas\\Pictures\\parts.png");
            image = ImageIO.read(pnginput);

            int i,j;
            int oneDimCounter=0;

            //get information
            int height= image.getHeight();
            int width = image.getWidth();

            int newWidth = width + (width % BLOCKSIZE);//make picture divisible by 8
            int newHeight= height + (height % BLOCKSIZE);
            //Color [][] colors = new Color [width][height];
            double [][] lumina = new double [newWidth][newHeight];
            double [][]dctInput= new double [BLOCKSIZE][BLOCKSIZE];
            double [][]dctResult=new double [BLOCKSIZE][BLOCKSIZE];//after the dct is calculated
            double [][]dctQuantized=new double[BLOCKSIZE][BLOCKSIZE];
            
            Number []huffTable = new Number[256];
            //initialize table
            for(int k=0;k<256;k++){
                huffTable[k].value = (byte) (k - 128);
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

                    lumina[i][j] = (double)((.299 * red) + (.578 * green) + (.114 * blue) -127);//<-- -127 to produce values around zero
                }
            }
            
            //fill the rest with zero
            for(i = width; i < newWidth ;i++){
                for(j = 0; j < newHeight;j++){
                    lumina[i][j] = 0;
                }
            }
            for(i = 0; i < width ;i++){
                for(j = height; j < newHeight;j++){
                    lumina[i][j] = 0;
                }
            }
            
            /*for(i=0; i<newWidth; i++){
                for(j=0; j<newHeight; j++){
                    System.out.print(lumina[i][j]+"\t");
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
                            dctInput[a][b] = lumina[xstart + a][ystart + b];
                        }
                    }

                    // Apply DCT to block
                    dctResult = calcDCT(dctInput);
                    
                    // quantize the block
                    dctQuantized = quantize(dctResult);

                    //huffman encode - convert to 1D array and count frequency
                    byte tempByte;
                    for (a = 0; a < BLOCKSIZE; a++) {
                        for (b = 0; b < BLOCKSIZE; b++) {
                            tempByte = (byte)dctQuantized[a][b];
                            oneDimArray[oneDimCounter] = tempByte;
                            huffTable[(tempByte+128)].occurance++;//add one to the occurance
                            oneDimCounter++;
                        }
                    }
                    //sort by highest occurance to lowest
                    for(int k=0;k<256;k++){
                        ;
                    }
                    
                    /*oneDimCounter=0;
                    boolean upward = true;
                    while(oneDimCounter != BLOCKSIZE*BLOCKSIZE){
                        int f=0,g=0;
                        oneDimArray[oneDimCounter++] = (byte)dctQuantized[f][g];
                        if(upward){
                            if(f==0){
                                upward = false;
                                g++;
                            }
                            else{
                                f--;
                                g++;
                            }
                        }
                        else{
                            if(g==0){
                                upward = true;
                                f++;
                            }
                            else{
                                g--;
                                f++;
                            }
                        }
                    }*/
                    
                    /*for (a = 0; a < BLOCKSIZE; a++) {
                        for (b = 0; b < BLOCKSIZE; b++) {
                            reconstImage[xstart + a][ystart + b] =(int) dctQuantized[a][b];//fill the new reconstructed image from the 8x8 block (dctArray4)
                        }
                    }*/
                    
                }
            }
            
            //for logic checking purposes
            for (i = 0; i < newWidth*newHeight; i++) { 
                System.out.print(oneDimArray[i]+"\t");
                if((i+1)% newWidth == 0)
                    System.out.print("\n");
            }
            

            //write the image to a file, resource: http://web.cecs.pdx.edu/~harry/compilers/ASCIIChart.pdf
            //http://www.file-recovery.com/jpg-signature-format.htm
            outputbytes[0]= -1;  //ff StartOfImage
            outputbytes[1]= -40; //d8 
            outputbytes[2]= -1;  //ff Application Marker
            outputbytes[3]= -32; //e0
            outputbytes[4]= 16; //Length
            outputbytes[5]= 16;
            
            outputbytes[6]= 74;//4a conforms to jfif
            outputbytes[7]= 70;//46
            outputbytes[8]= 73;//49
            outputbytes[9]= 70;//46
            outputbytes[10]= 0;//00
            
            
            outputbytes[40-1]= -1;//ff endOfImage
            outputbytes[40]= -39;//d9
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

    private static double[][] calcDCT(double [][] dctarray){

        double[][] buffer = new double[BLOCKSIZE][BLOCKSIZE];

        for (int u = 0; u < BLOCKSIZE; u++){
            for (int v = 0; v < BLOCKSIZE; v++){
                
                buffer[u][v] = (double)(.25) * edgecase((int) u) * edgecase((int) v);//may need to cast to double
                //System.out.print(buffer[u][v]+"\t");
                double xysum = 0;
                for (int x = 0; x < BLOCKSIZE; x++){
                    for (int y = 0; y < BLOCKSIZE; y++){
                        //System.out.print(dctarray[x][y]+"\t");
                        xysum += dctarray[y][x]//flip for transpose
                                      * Math.cos(((2 * x + 1) * u * Math.PI) / 16.0)
                                      * Math.cos(((2 * y + 1) * v * Math.PI) / 16.0);
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
    
    private static double edgecase(int index){// <-- <-- <-- don't forget to rename this!!! <-- <-- <--
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
                quantum[i][j] = dctarray[i][j]/Math.floor((S(quality)*quantMatrix[i][j]+50)/100);
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
