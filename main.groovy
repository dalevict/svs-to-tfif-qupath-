/**
 * export_all_images.groovy  (v4 — OMEPyramidWriter direct import)
 *
 * HOW TO RUN:
 *   Import all .svs images into a project (project folder needs to be in 
 *   the same folder as this file). Select H-DAB Brightfield to import all the 
 *   images (doesn't need to be 4). You don't need to drag them into the
 *   viewing grid or create an ROI, the script does it automatically (selects
 *   the entire images as the ROI).
 *   Automate → Show Script Editor → open this file → Run for project
 *
 * STRUCTURE OF FOLDER:
 * <parent folder>
 *   <this file>
 *   <project folder>
 *      classifiers (created by Qupath)
 *      data        (created by Qupath)
 *      exports     (created by Qupath)
 *          <ImageNameWithoutExtension>_ROI.ome.tif
 * 
 * The output file for any given image will be:
 *   <project_folder>/exports/<ImageNameWithoutExtension>_ROI.ome.tif
 *
 * REQUIRES: QuPath 0.3.x or later (OMEPyramidWriter ships with QuPath core)
 * If you get a ClassNotFoundException, check Help → About to confirm version.
 */

// ─── CONFIGURATION ───────────────────────────────────────────────────────────

double  DOWNSAMPLE     = 1.0    // 1 = full res, 2 = half resolution, 4 = quarter
boolean SKIP_IF_EXISTS = true   // safe to re-run; won't redo finished exports
int     TILE_SIZE      = 512    // tile size in pixels for the OME-TIFF

// ─────────────────────────────────────────────────────────────────────────────

import qupath.lib.images.ImageData
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.roi.ROIs
import qupath.lib.common.GeneralTools
import qupath.lib.images.writers.ome.OMEPyramidWriter
import qupath.lib.images.writers.ome.OMEPyramidWriter.CompressionType

def project    = getProject()
def projectUri = project.getPath()
print "Project URI: ${projectUri}"

def projectDir = new File(projectUri.toFile().getParent())
print "Project dir: ${projectDir.getAbsolutePath()}"

def exportDir = new File(projectDir, "exports")
exportDir.mkdirs()
print "Export dir : ${exportDir.getAbsolutePath()}"
print "Export dir writable: ${exportDir.canWrite()}"
print ""

int exported = 0, skipped = 0, errors = 0

project.getImageList().each { entry ->
    def imageName = GeneralTools.stripExtension(entry.getImageName())
    def outFile   = new File(exportDir, "${imageName}_ROI.ome.tif")

    print "─────────────────────────────────────"
    print "[Image] ${entry.getImageName()}"
    print "  Output path: ${outFile.getAbsolutePath()}"

    if (SKIP_IF_EXISTS && outFile.exists()) {
        print "  → SKIP (already exported)"
        skipped++
        return
    }

    ImageData imageData = null
    try {
        imageData = entry.readImageData()
    } catch (Exception e) {
        print "  ERROR reading image data: ${e.class.simpleName}: ${e.message}"
        errors++
        return
    }

    def server = imageData.getServer()
    print "  Server class : ${server.getClass().getSimpleName()}"
    print "  Image size   : ${server.getWidth()} x ${server.getHeight()} px"

    try {
        if (imageData.getImageType() == ImageData.ImageType.UNSET) {
            imageData.setImageType(ImageData.ImageType.BRIGHTFIELD_H_DAB)
            print "  → Image type set to H-DAB Brightfield"
        }
        def hierarchy = imageData.getHierarchy()
        if (hierarchy.getAnnotationObjects().isEmpty()) {
            def roi = ROIs.createRectangleROI(
                0, 0, server.getWidth(), server.getHeight(), null)
            def ann = new PathAnnotationObject(roi)
            ann.setName("Full Image ROI")
            hierarchy.addObject(ann)
            print "  → Created full-image annotation"
        }
        print "  → Starting tiled OME-TIFF export…"

        new OMEPyramidWriter.Builder(server)
            .downsamples(DOWNSAMPLE)
            .tileSize(TILE_SIZE)
            .compression(CompressionType.LZW)
            .build()
            .writeSeries(outFile.getAbsolutePath())

        if (outFile.exists()) {
            print "  ✓ Exported (${(outFile.length() / 1_048_576).round(1)} MB)"
            entry.saveImageData(imageData)
            exported++
        } else {
            print "  ERROR: export finished but file not found on disk!"
            errors++
        }

    } catch (Exception e) {
        print "  ERROR during export: ${e.class.simpleName}: ${e.message}"
        e.printStackTrace()
        errors++
    }
}

print ""
print "="*50
print "Done.  Exported: ${exported}  |  Skipped: ${skipped}  |  Errors: ${errors}"
print "Output folder: ${exportDir.getAbsolutePath()}"
print "="*50