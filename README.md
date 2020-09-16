# Отслеживание цветного шарика с использованием OpenCV

В данном проекте реализован отслеживание цветного шарика на изображении с вебкамеры. Да-да, именно только шарика. Во многих проектах, что удавалось найти на просторах интернета, присутствует просто отслеживание цветного объекта, но любого объекта - не только шарика. Проект сделан на Java (в отличие также об большинства проектов в интернете, сделанных на Python) с использованием библиотеки OpenCV.

Запущенное приложение представляет из себя просто окошко с изображением с вебкамеры, на котором желтыми точками рисуется траектория движения цветного шарика. В данном проекте подразумевается отслеживание оранжевого шарика. Поменяв значения HSV порогов в коде, можно отслеживать и объекты другого цвета.

В режиме дебага (переменная в коде) открывается три окна. Первое окно - желтыми точками рисуется траектория движения цветного шарика, синий круг вокруг контура, который распознался как круг, и красным рисуются реальные контуры, по которым происходила попытка определения является ли объект кругом с помощью метода Хафа. Второе окно - изображение с камеры, переведенное в HSV. Третье окно - маска, которая осталась после выделенных объектов (оранжевых).

# Color Ball Tracking with OpenCV

In this project, the tracking of a colored ball on the image from the webcam is implemented. Yes, just a ball. In many projects that we could find on the Internet, there is simply tracking a colored object, but any object - not just a ball. The project is made in Java (unlike most projects on the Internet made in Python) using the OpenCV library.

The launched application is just a window with an image from a webcam, on which the trajectory of a colored ball is drawn with yellow dots. In this project, the tracking of the orange ball is meant. By changing the HSV thresholds in the code, you can track objects of a different color.

In debug mode (variable in the code), three windows are opened. The first window - yellow dots draws the trajectory of a colored ball, a blue circle around the contour, which was recognized as a circle, and real contours are drawn in red, along which an attempt was made to determine whether an object is a circle using the Hough method. The second window is the image from the camera converted to HSV. The third window is the mask that remains after the selected objects (orange).

P.S. This is translation by Google Translator.
