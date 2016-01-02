# imageviewer

##Overview
The CDSC/MII imageViewer is a Java-based medical image viewing application that is written to be a modular image manipulation and visualization platform that can be executed for any computer running an instance of the Java Virtual Machine. The application has networking capabilities that can be tied to an ImageServer network to pull images from a local area network or an enterprise picture archiving and communication system (PACS). It also includes several 2D segmentation algorithms (front propagation, active contours). ImageViewer is also being extended to support Java 3D volume rendering, the caBIG Annotation Image Markup project, and the RSNA MIRC service. These features are disabled in the current release but will be enabled as testing of these components are completed.

The ImageViewer application can be adapted as a testbed for new image processing algorithms as well as integrated into existing Java applications as a robust, integrated image viewer tool. Documentation is provided on how to write a new plugin for the application or how to import project files into an existing application.

##Features
Basic image manipulation functionality (window/level, zoom, rotation)
Advanced layout capabilities (multiple tabs, custom layouts 2x2, 2x4 and side-by-side tabs)
Support for common medical imaging formats (DICOM, Analyze 7.5, raw images, and visual human dataset (VHD) images)
Several built-in 2D segmentation algorithms: geodesic active contours, front propagation
Integrated support for ImageServer
Requirements
Java Virtual Machine Standard Edition 1.6+
Minimum 256 MB RAM, 512 MB recommended

##Download
Two methods are available for obtaining and running the standalone ImageViewer code. If you are looking for the image processing pipeline, you can download a version of imageViewer with the pipeline pre-integrated from the other repository.

Run from Precompiled Archive
Download the ImageViewer distribution package.

Once downloaded, to start the application, execute the following command:

java -Xms256m -Xmx1024m -jar imageviewer.jar -nonet

You may also run the application with the -help trigger to list other arguments that you may specify at command line.

Compile from Source Code
The checked out project can be imported into Eclipse. The main method is located in imageviewer.system.ImageViewerClient.

You may also compile the code with the included build.xml file using ant. Make sure you properly configure your environment by specifying your JAVA_HOME variable (i.e., path to your Java Virtual Machine).
