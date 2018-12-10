package paint;

import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import static javafx.embed.swing.SwingFXUtils.fromFXImage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Moses Cuevas
 */
public class paintfxmlController implements Initializable {

    //for testing
    long timer1start, timer1end, test1time;
    long timer2start, timer2end, test2time;
    long timer3start, timer3end, test3time;
    
    double size = 6;
    
    @FXML
    private ColorPicker colorpicker;
    
    @FXML
    private TextField bsize;
    
    @FXML
    private Button btn_draw, btn_more ,btn_less;
    
    @FXML
    private Canvas canvas;
    
    @FXML
    private Rectangle recentcolor1,recentcolor2,recentcolor3,
            recentcolor4,recentcolor5,recentcolor6;
    
    boolean toolSelected = false;
    
    GraphicsContext brushTool;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recentcolor1.setFill(Color.GREEN);
        recentcolor2.setFill(Color.YELLOW);
        recentcolor3.setFill(Color.PURPLE);
        recentcolor4.setFill(Color.ORANGE);
        recentcolor5.setFill(Color.RED);
        recentcolor6.setFill(Color.BLUE);
        
        colorpicker.setValue(Color.BLUE);
        
        brushTool = canvas.getGraphicsContext2D();
        canvas.setOnMouseDragged(e -> {
            double size = Double.parseDouble(bsize.getText());
            double x = e.getX() - size / 2;
            double y = e.getY() - size / 2;
            
            if(toolSelected && !bsize.getText().isEmpty()){
                brushTool.setFill(colorpicker.getValue());
                brushTool.fillRoundRect(x, y, size, size, size, size);
            }
        });
        
        colorpicker.setOnMouseClicked(new EventHandler<MouseEvent>(){//needs work <-- <-- <-- <-- <--
            @Override
            public void handle(MouseEvent t){
                recentcolor6.setFill(recentcolor5.getFill());
                recentcolor5.setFill(recentcolor4.getFill());
                recentcolor4.setFill(recentcolor3.getFill());
                recentcolor3.setFill(recentcolor2.getFill());
                recentcolor2.setFill(recentcolor1.getFill());
                recentcolor1.setFill(colorpicker.getValue());
            }
        });
        /*
        btn_more.setOnMouseClicked(new EventHandler<MouseEvent>(){//needs work <-- <-- <-- <-- <--
            @Override
            public void handle(MouseEvent t){
                size=size+1;
            }
        });
        
        btn_less.setOnMouseClicked(new EventHandler<MouseEvent>(){//needs work <-- <-- <-- <-- <--
            @Override
            public void handle(MouseEvent t){
                size=size-1;
            }
        });*/
    }    
    
    @FXML
    public void newcanvas(ActionEvent e){
        TextField getCanvasWidth = new TextField();
        getCanvasWidth.setPromptText("Width");
        getCanvasWidth.setPrefWidth(150);
        getCanvasWidth.setAlignment(Pos.CENTER);
        
        TextField getCanvasHeight = new TextField();
        getCanvasHeight.setPromptText("Height");
        getCanvasHeight.setPrefWidth(150);
        getCanvasHeight.setAlignment(Pos.CENTER);
        
        Button createButton = new Button();
        createButton.setText("Create Canvas");
        
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(getCanvasWidth, getCanvasHeight,createButton);
        
        Stage createStage = new Stage();
        AnchorPane root = new AnchorPane();
        root.setPrefWidth(200);
        root.setPrefHeight(200);
        
        root.getChildren().add(vbox);
        
        Scene CanvasScene = new Scene (root);
        createStage.setTitle("Create Canvas");
        createStage.setScene(CanvasScene);
        createStage.show();
        
        createButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle (ActionEvent event){
                double canvasWidthReceived = Double.parseDouble(getCanvasWidth.getText());
                double canvasHeightReceived = Double.parseDouble(getCanvasHeight.getText());
                
                canvas = new Canvas();
                canvas.setWidth(canvasWidthReceived);
                canvas.setHeight(canvasHeightReceived);
                
                vbox.getChildren().add(canvas);
                createStage.close();
            }
        });
    }
    
    @FXML
    public void toolselected(ActionEvent e){
        toolSelected = true;   
    }
    
    @FXML
    public void saveJpeg(ActionEvent e){
        /*BufferedImage bimage = new BufferedImage(canvas.getWidth(), canvas.getHeight(),BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimage.createGraphics();
        canvas.paint(g);*/
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.WHITE);//can set to TRANSPARENT
        
        WritableImage wimage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        canvas.snapshot(sp,wimage);
        
        //File file = new File("testFromJavaPaint.jpg");
        //BufferedImage bimage = (SwingFXUtils.fromFXImage(img, null))
        
        //BufferedImage bimage = (SwingFXUtils.fromFXImage(wimage, null));
        BufferedImage bimage = fromFXImage(wimage, null);
        
        
        SaveAsJPG jpegHandler= new SaveAsJPG(bimage);
        
        /*try {
            ImageIO.write(SwingFXUtils.fromFXImage(wimage, null), "jpg", file);//remove this
        } catch (IOException ex) {
            // TODO: handle exception here
        }*/
    }
    
    @FXML
    public void savePng(ActionEvent e){

        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.WHITE);//can set to TRANSPARENT
        
        WritableImage wimage = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        canvas.snapshot(sp,wimage);
        
        //File file = new File("testFromJavaPaint.png");
        //BufferedImage bimage = (SwingFXUtils.fromFXImage(wimage, null));
        BufferedImage bimage = fromFXImage(wimage,null);
        BufferedImage bimageByte = new BufferedImage(bimage.getWidth(), bimage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        
        bimageByte.getGraphics().drawImage(bimage, 0, 0, null);
        /*try {
            ImageIO.write(SwingFXUtils.fromFXImage(wimage, null), "png", file);//remove this
        } catch (IOException ex) {
            // TODO: handle exception here
        }*/
        
        //saveasPng pngHandler = new saveasPng();
        int[][] result = saveasPng.PngGetPixels(bimageByte);
        try (OutputStream out = new FileOutputStream("PngOutput.png")) {
            saveasPng.write(result, out);
        }
        catch(Exception f){}
    }
    
    @FXML
    public void addone(ActionEvent e){
        double size = Double.parseDouble(bsize.getText());
        size = size + 1;
        bsize.setText(size+"");//set to string
    }
    
    @FXML
    public void subone(ActionEvent e){
        double size = Double.parseDouble(bsize.getText());
        if(size > 1){
            size = size - 1;
            bsize.setText(size+"");//set to string
        }
    }
    
    /*@FXML
    public void test(ActionEvent e){//for testing purposes only - can be removed later
        Button btnTest1 = new Button();
        btnTest1.setText("Test 1");
        
        Button btnTest2 = new Button();
        btnTest2.setText("Test 2");
        
        Button btnTest3 = new Button();
        btnTest3.setText("Test 3");
        
        Stage createStage = new Stage();
        AnchorPane root = new AnchorPane();
        root.setPrefWidth(200);
        root.setPrefHeight(200);
        
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setLayoutX(70);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(btnTest1, btnTest2, btnTest3);
        
        root.getChildren().add(vbox);
        
        Scene CanvasScene = new Scene (root);
        createStage.setTitle("Create Canvas");
        createStage.setScene(CanvasScene);
        createStage.show();
        
        btnTest1.setOnAction(new EventHandler<ActionEvent>(){//draw a 1 in diameter circle and fill it
            @Override
            public void handle (ActionEvent event){
                createStage.close();
                timer1start = System.currentTimeMillis();
                System.out.println("Test 1 start: "+ timer1start);
                
                btn_draw.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                    timer1end = System.currentTimeMillis();
                    System.out.println("  Test 1 end: "+ timer1end);
                    test1time = timer1end - timer1start;
                    System.out.println("Test 1 time: "+ test1time);
                });
            }
        });
        
        btnTest2.setOnAction(new EventHandler<ActionEvent>(){//draw a line using bezier tool
            @Override
            public void handle (ActionEvent event){
                
                createStage.close();
                timer2start = System.currentTimeMillis();
                
                timer2end = System.currentTimeMillis();
                test2time = timer2end - timer2start;
            }
        });
        
        btnTest3.setOnAction(new EventHandler<ActionEvent>(){//select a color and draw a 1 inch square
            @Override
            public void handle (ActionEvent event){
                
                createStage.close();
                timer1start = System.currentTimeMillis();
                
                timer3end = System.currentTimeMillis();
                test3time = timer3end - timer3start;
            }
        });
    }*/
}
