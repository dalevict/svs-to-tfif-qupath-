# svs-to-tfif-qupath-
Qupath .groovy script for batch converting .svs images to .tfif

# HOW TO RUN:
* Import all .svs images into a project (project folder needs to be in  the same folder as this file).
* Select H-DAB Brightfield to import all the images (doesn't need to be 4). You don't need to drag them into the viewing grid or create an ROI, the script does it automatically (selects the entire images as the ROI).
* Automate → Show Script Editor → open this file → Run for project

# STRUCTURE OF FOLDER:
 <parent folder>
   <this file>
   <project folder>
      classifiers (created by Qupath)
      data        (created by Qupath)
      exports     (created by Qupath)
         <ImageNameWithoutExtension>_ROI.ome.tif

           
# The output file for any given image will be:
<project_folder>/exports/<ImageNameWithoutExtension>_ROI.ome.tif
REQUIRES: QuPath 0.3.x or later (OMEPyramidWriter ships with QuPath core)
If you get a ClassNotFoundException, check Help → About to confirm version.
