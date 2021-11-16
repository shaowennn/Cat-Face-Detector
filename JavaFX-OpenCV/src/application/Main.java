package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class Main extends Application
{
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			// 加载FXML文件的资源 引用FirstJFX.fxml文件
			FXMLLoader loader = new FXMLLoader(getClass().getResource("FirstJFX.fxml"));
			// 保存loader给后面调用
			BorderPane rootElement = (BorderPane) loader.load();
			// 创建一个场景 scene 大小 800*600
			Scene scene = new Scene(rootElement, 800, 600);
			// 设置scene的css样式 引用application.css文件
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			// create the stage with the given title and the previously created
			// scene
			
			// 设置标题和内容
			primaryStage.setTitle("猫脸识别");
			primaryStage.setScene(scene);
			// show the GUI
			
			// 显示界面
			primaryStage.show();
			
			// init the controller
			
			// 初始化控制器
			final FXController controller = loader.getController();
			
			// Set video device
			// 获取第一个摄像头设备的编号(默认是1)
			int videodevice = 1;
			
			//*** 重点 ***///
			// 调用控制器当中的初始化方法initController 把摄像头编号传过去
			controller.initController(videodevice);
			
			// GUI界面上的关闭按钮的响应事件
			primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {
		        	  // 调用controller的关闭方法setClosed
		        	  controller.setClosed();
		        	  System.out.println("Closed");
		          }
		      })); 
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * For launching the application...
	 */
	public static void main(String[] args)
	{
		// load the native OpenCV library
		// 加载原生OpenCV库
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// 调用上面那个函数
		launch(args);
	}
}