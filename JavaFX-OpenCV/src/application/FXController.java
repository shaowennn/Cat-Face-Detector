package application;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class FXController {
	// the FXML button
	@FXML
	private Button button;
	// the FXML image view
	@FXML
	private ImageView currentFrame;

	@FXML
	private Text fps;

	@FXML
	private Pane ImageViewPane;

	// the OpenCV object that realizes the video capture
	// 一个OpenCV的对象 用来渲染出摄像头画面
	private VideoCapture capture = new VideoCapture(0);
	// a flag to change the button behavior
	// 一个标记 用来判断按钮是否被按下
	private static boolean cameraActive = false;

	// Settings

	// Set video device
	// 设置视频设备
	
	// CascadeClassifier，是Opencv中做人脸检测的时候的一个级联分类器 既可以使用Haar，也可以使用LBP特征。
	private CascadeClassifier face_cascade;
    private long detectedFaces;
    public BufferedImage croppedImage;

    
    // 初始化函数
	public void initController(int vd) {
		
		//级联分类器 存储 haarcascade_frontalcatface.xml 猫脸的相关特征 用绝对路径引用进来
		face_cascade = new CascadeClassifier("C:/Users/Djokovic/Downloads/opencv/opencv/sources/data/haarcascades/haarcascade_frontalcatface.xml"); 
		//补充 人脸的特征文件是 haarcascade_frontalface_default.xml
		
		//判断 级联分类器 是否加载成功 如果empty的返回值为true则代表没加载到 xml文件
        if(face_cascade.empty()){  
             System.out.println("--(!)Error loading A\n");  
             return;  
        }  
        else{  
             System.out.println("Face classifier loooaaaaaded up");  
        }  
	}

	/**
	 * The action triggered by pushing the button on the GUI
	 * 
	 * 在界面上的startCamera按钮被按下，就会触发startCamera这个函数
	 * 
	 * 对应在 FirstJFX.fxml当中 24行 onAction="#startCamera" 这个语句被调用
	 * 
	 * @param event
	 *            the push button event
	 */
	@FXML
	protected void startCamera(ActionEvent event) {
		try {
			if (!cameraActive) {//cameraActive是boolean类型的，true表示摄像头打开，false表示摄像头关闭
				// start the video capture
				//this.capture.open(videodevice);

				// is the video stream available?
				// this.capture.isOpened() 表示是视频流是否被打开 是否可以使用
				if (this.capture.isOpened()) {
					cameraActive = true;
					
					//以一个线程的形式来启动 
					// 为什么用线程不用函数？因为函数会阻塞代码，要等到函数return了才会继续往下执行
					// 而线程不需要等待它结束 可以多个同时执行
					
					// processFrame 是每一帧执行识别的代码 为了不让它卡顿 所以使用了线程
					Thread processFrame = new Thread(new Runnable() {
						
						//重载 系统原来有这个函数 我们现在又重写一遍用来覆盖它的默认函数
						@Override
						public void run() {
							while (cameraActive) {
								try {
									
									// 相关函数说明文档在线地址 https://www.w3cschool.cn/opencv/
									
									//Grab Frame
									// material 布料 材质  matToShow 用来展示出来的底板 toShow
									// 刚开始用默认的图像帧 grabFrame 赋值 
									Mat matToShow = grabFrame();
									
									//Process Frame
									//使用该函数产生一个图象帧 返回值是Image对象 这是固定用法
									matToShow = processMat(detect(matToShow));
									
									// convert the Mat object (OpenCV) to Image (JavaFX)
									// 把OpenCV的Mat对象 转换成 JavaFX这个UI库的对象
									Image imageToShow = mat2Image(matToShow);
									
									//Update ImageView
									//主线程每帧更新
									setFrametoImageView(imageToShow);
									
									//Update the UI
									// JavaFX更新界面的函数
									updateUIObjects();

								} catch (Exception e1) {
									System.out.println("Error on Update " + e1);
								}
							}
							System.out.println("Thread processFrame closed");
							try {
								// release 释放摄像头资源
								capture.release();
								
								// JavaFX更新界面的函数
								updateUIObjects();
								
								//主线程每帧更新 设置为null 不更了 收工
								setFrametoImageView(null);
							} catch (Exception e) {
							}

						}

					});
					
					//守护线程--也称“服务线程”，在没有用户线程可服务时会自动离开。优先级：守护线程的优先级比较低
					//通过setDaemon(true)来设置线程为“守护线程”
					processFrame.setDaemon(true);
					//设置该线程的名字
					processFrame.setName("processFrame");
					//设置该线程开始
					processFrame.start();

					// 设置按钮的文字
					this.button.setText("Stop Camera");
				} else {
					// log the error
					throw new Exception("Impossible to open the camera connection");
				}
			} else {
				// the camera is not active at this point
				cameraActive = false;
				// update again the button content
				this.button.setText("Start Camera");
			}
		} catch (Exception e) {
			e.printStackTrace();
			cameraActive = false;
			this.button.setText("Start Camera");
		}
	}

	/**
	 * Always Update UI from main thread
	 * 在JavaFX的主线程当中跟新UI的函数
	 */
	private void setFrametoImageView(Image frame) {
		Platform.runLater(() -> {
			currentFrame.setImage(frame);
			currentFrame.setFitWidth(ImageViewPane.getWidth());
			currentFrame.setFitHeight((ImageViewPane.getHeight()));
			// set Image height/width by window size
		});

	}
	
	//这个是和OpenCV打交道的 重点掌握
	public Mat detect(Mat inputframe){  
        Mat mRgba=new Mat();  //mRgba是指 存储了Red Green Blue Alpha （透明度） 的 material
        Mat mGrey=new Mat();  //mGrey是指 Grey 空的 material
        MatOfRect faces = new MatOfRect();  //faces是识别到物体的矩形对象 Reat
        
        inputframe.copyTo(mRgba);  // 把传入的图象拷贝到mRgba当中处理
        inputframe.copyTo(mGrey);    // 把传入的图象拷贝到mGrey当中处理
        
        Imgproc.cvtColor( mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);  //使用cv :: cvtColor将图像从BGR转换为灰度格式
        Imgproc.equalizeHist( mGrey, mGrey );  //通过使用OpenCV函数cv :: equalizeHist来均衡图像的直方图
        
        face_cascade.detectMultiScale(mGrey, faces);  //LBP分类器cv :: CascadeClassifier :: detectMultiScale来执行检测。
        detectedFaces = faces.toArray().length; // long数据类型 代表我检测出多少个样本
        
        System.out.println(String.format("Detected %s faces", detectedFaces)); //输出我识别了多少个 detectedFaces 样本 
        
        for(Rect rect:faces.toArray()){  
             Point center= new Point(rect.x + rect.width*0.5, rect.y + rect.height*0.5 ); //求出矩形rect的中心点位置 
             Imgproc.ellipse( mRgba, center, new Size( rect.width*0.5, rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );//ellipse. 椭圆
             //画出椭圆 把识别到的物体圈了起来
             //Core.rectangle(mRgba, new Point(rect.width*0.5, rect.height*0.5), center, new Scalar( 0, 255, 255 ), 4, 8, 0);
             croppedImage = Mat2BufferedImage(mGrey);
        }  
        return mRgba;  
   }
   
	//这个函数是把opencv的mat转成fx的BufferedImage类型
   public BufferedImage Mat2BufferedImage(Mat m){
   	// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
   	// Fastest code
   	// The output can be assigned either to a BufferedImage or to an Image

   	    int type = BufferedImage.TYPE_BYTE_GRAY;
   	    if ( m.channels() > 1 ) {
   	        type = BufferedImage.TYPE_3BYTE_BGR;
   	    }
   	    int bufferSize = m.channels()*m.cols()*m.rows();
   	    byte [] b = new byte[bufferSize];
   	    m.get(0,0,b); // get all the pixels
   	    BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
   	    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
   	    System.arraycopy(b, 0, targetPixels, 0, b.length);  
   	    return image;

   	}

	/**
	 *总在线程中更新UI （这个是一直更新ui上的文字 显示当前fps给用户看）
	 */
	private void updateUIObjects() {
		Platform.runLater(() -> {
			// Update UI Objects like: Textfield.setText() , Button.set..() ,
			// Window.Resize...()
			//Set FPS
			fps.setText(""+capture.get(5));
		});
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *（刚开始调用的时候要临时搞个背景则生成一个空白帧凑数）
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame() {//grabFrame 生成空白帧
		// init everything
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					
				}

			} catch (Exception e) {
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	/**
	 * 返回一个frame
	 *
	 * @return the {@link Image} to show
	 */
	private Mat processMat(Mat matToShow) {
		// convert the image to gray scale
		//Imgproc.cvtColor(matToShow, matToShow, Imgproc.COLOR_BGR2GRAY);
		return matToShow;
	}

	/**
	 *opencv的mat类型 转 javafx的buffered Image类型（这个用法作为不同库之间数据类型的转换）
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		try {
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		} catch (Exception e) {
			System.out.println("Cant convert mat" + e);
			return null;
		}
	}

	public BufferedImage matToBufferedImage(Mat matBGR) {
		int width = matBGR.width(), height = matBGR.height(), channels = matBGR.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		matBGR.get(0, 0, sourcePixels);
		BufferedImage image;
		if (matBGR.channels() > 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		return image;
	}

	public void setClosed() {
		//Close thread on window close
		cameraActive = false;
	}

}
