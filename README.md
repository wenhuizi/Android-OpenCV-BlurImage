背景意义
   =	
   
研究背景
  ==
你是否还在为手机相册里有很多模糊照片而烦心？
在我们日常生活中拍照记录时，经常会拍到一些重复的图片，这些图片中不乏模糊到看不清的图片，在后期看相册时会发现这些图片非常烦心。因此设计了一款能够检测图片模糊度并实现批量删除模糊图片的APP。

研究意义
=
1. 提高照片质量：模糊照片会影响观看体验，通过删除模糊照片，可以提高整体图片库的质量；
2. 节省存储空间：模糊照片通常没有太大的保留价值，删除他们可以节省存储空间；
3. 提高效率：在查找或整理照片时，模糊照片可能会浪费用户的时间。通过自动删除模糊照片，可以提高用户的效率；
4. 优化用户体验：用户不再需要手动筛选和删除模糊图片，提高了用户体验。

开发工具
=
系统开发平台:  Android Studio2022.3.1
系统开发语言:  Java
调用依赖库：  计算机视觉库OpenCV 4.5.5 
              图片选择库Glide
		      图片选择库 PictureSelector
              权限请求框架 XXPermissions
	      
基本功能描述
=

基本功能
==
1. 模糊检测:
	应用程序使用拉普拉斯方差判断方法进行图像处理来分析照片的清晰度。用户只需将照片导入应用程序，它将自动进行模糊度分析并给出结果。
2. 拍照保存照片
	获取拍照权限，使用拍照功能，拍照后进行模糊度检测计算，用户根据计算出的模糊度结果选择是否保存图片。
3. 批量删除模糊图片
	通过打开相册，批量选择选择要识别的图片集，选择后将自动删除模糊图片，更新系统相册。

功能模块图
=
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/6680a574-f222-4ec6-b662-79d0352a9279)

项目设计
=

设计步骤
=
1.	结合自身生活确定选题，确定将要实现的功能。
2.	查阅相关资料，确定模糊判断算法计算方法。
3.	安装配置所用到的依赖库。
4.	设计简单页面布局。
5.	编写申请相机拍照权限和相册保存和删除代码。
6.	编写模糊度计算代码。
7.	代码调试以及真机测试调式。

项目结构说明
=
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/d8365fba-a017-4902-a1f4-2c26ff4e19fc)
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/32c798af-db60-48a9-9600-8d81c4f082f5)

不同文档的作用
=
1.	activity
MainActivity:实现拍照后将图片进行模糊度检测，给出清晰度，由用户判断是否保存图片。
SelectImg:实现打开相册选择多张图片的功能，可以将选择后的图片进行模糊度计算，给出清晰度集合，由用户判断是否删除判断为“非常不清晰”和“完全看不清”的图片。
2.	detect
Blur2Detect:使用OpenCvUtil获得模糊度。
3.	util工具类
CameraUtil:调用手机相机功能进行拍照；
GlideEngine:通过uri使图片加载显示给用户。
OpenCvUtil:使用Opencv库获得图像数据，在调用其中拉普拉斯函数计算其拉普拉斯方差，并给出清晰程度。
4.	接口类
两个回调，回调计算结果。
5.	layout
activity_main.xml主界面设计（其中包括一个ImageButton，可以将刚刚拍摄的照片加载在此控件上）。
activity_select_img.xml打开相册后的界面。
dialog_blur.xml拍摄照片后反馈模糊度与提示是否保存的弹窗。

界面设计
=
主界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/bd251a7a-fc76-4478-9d24-d91ca4e7b5d1)
拍照界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/a2b9ad4b-0bca-4e48-a328-f9324ac73c41)
拍照后反馈界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/4061187a-2a0f-4b54-935a-f0fb18187147)
保存成功
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/a701fbe4-00de-447f-aa0c-8d00a6bc9f5a)
打开相册界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/9d0195c2-ac01-42a1-a2a2-0f00afdc9d4d)
选择多张图片界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/a248e8eb-8941-4f8e-b457-7345752c2639)
检测结果反馈界面
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/f7b674d9-c96b-4fe5-a3ee-b6be67c7d679)
询问用户是否删除
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/1990a91d-ff3e-4ad5-bb9d-aaa607622115)
删除成功
![image](https://github.com/wenhuizi/Android-OpenCV-BlurImage/assets/169887674/8f619f8b-8325-45a0-a971-0246fc1ec678)


结论与心得：
=
耗时数周，终于完成了从想法到实施都由自己思考的安卓项目。从开始的OpenCV库安装导入耗时三周，到调试bug时的崩溃，到最后项目基本成功的开心，对我的心态也是一个磨练。
但同时此项目也有好多不足之处，比如：
1.	使用拉普拉斯方差判断模糊度算法的阈值设置还不够准确；
2.	由于花费大量时间实现模糊度判断算法，因此页面布局设计的较为简陋，但还算美观；（不过界面清晰简单也可以是优点）
3.	不够细心，平时学习不够踏实，对于开发语言的使用不够熟练，在开发过程中遇到了许多困难。
