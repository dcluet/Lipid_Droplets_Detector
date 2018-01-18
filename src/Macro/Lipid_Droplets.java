macro "Lipid_Droplets"{

//Key parameters
seuil = 5;
Iterations = 5;
SizeMin = 20;
SizeMax = 2000;
zDistance = 5;
enlargement = 5; //3 thus far
//Clean roiManager
roiManager("reset");

    //Choose image file and open it
    Path = File.openDialog("Choose file");
    open(Path);

    //Remove the non pixel unit
    run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=1.0000000");
    //Get the size of the current image
    W = getWidth();
    H = getHeight();
    T = getTitle();

    //Duplication of the image
    makeRectangle(0,0,W,H);
    run("Duplicate...", "title=Raw duplicate");

    //Create the Crop movie
    waitForUser("Set on the starting slice");
    Sstart = getSliceNumber();
    waitForUser("Set on the ending slice");
    Send = getSliceNumber();

    //Crop the Stack
    PathM1 = getDirectory("macros")
    PathM1 += "Droplets"+File.separator
    PathM1 += "Stack_Editing.java"

    ARG1 = "Raw" + "\t";
    ARG1 += "" + Sstart + "\t";
    ARG1 += "" + Send + "\t";

    runMacro(PathM1, ARG1);

    //Draw Neuropil
    selectWindow("Raw");
    run("Enhance Contrast", "saturated=0.35");
    waitForUser("Draw the neuropil");
    getSelectionCoordinates(NeuroPilX, NeuroPilY);
    resetMinAndMax();
    roiManager("reset");

    /*
        Close everything
        strat setbatchMode
        Reopen and redo
    */

    selectWindow(T);
    run("Close");
    selectWindow("Raw");
    run("Close");

    //Clean roiManager
    selectWindow("ROI Manager");
    run("Close");

    //setBatchMode(true)
    open(Path);
    roiManager("reset");
    //Remove the non pixel unit
    run("Properties...", "channels=1 slices="+nSlices()+" frames=1 unit=pixel pixel_width=1 pixel_height=1 voxel_depth=1.0000000");

    makeRectangle(0,0,W,H);
    run("Duplicate...", "title=Raw duplicate");

    runMacro(PathM1, ARG1);

    //Prepare Report
    run("Duplicate...", "title=Report duplicate");
    run("RGB Color");

    //Process the image to detect the lipid droplets
    selectWindow("Raw");
    makeRectangle(0,0,W,H);
    run("Gaussian Blur...", "sigma=1 stack");
    run("Maximum...", "radius=5 stack");

    nROI = 0;

    //Iteratively detect the strongest particles and remove them
    for (it=1; it<=Iterations; it++){

        selectWindow("Raw");
        makeRectangle(0,0,W,H);
        run("Duplicate...", "title=Temp duplicate");
        makeRectangle(0,0,W,H);
        run("Convert to Mask", "method=MaxEntropy background=Dark calculate");
        run("Analyze Particles...", "size="+SizeMin+"-"+SizeMax+" add stack");

        selectWindow("Temp");
        run("Close");

        if (nROI<roiManager("count")){


            //Remove all twins
            PathM2 = getDirectory("macros");
            PathM2 += "Droplets"+File.separator();
            PathM2 += "Twins_Killer.java";

            ARG2 = "Raw" + "\t";
            ARG2 += "" + nROI + "\t";
            ARG2 += "" + it + "\t";
            ARG2 += "" + seuil + "\t";
            ARG2 += "" + zDistance + "\t";
            ARG2 += "" + enlargement + "\t";
            runMacro(PathM2, ARG2);

            //Update the total number of ROI validated
            nROI = roiManager("count");
        }
    }// End of Iterations

    newImage("Neuropil", "8-bit white", 2048, 2048, 1);
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    setForegroundColor (0,0,0);
    run("Fill");
    for(i=0; i<roiManager("count"); i++){
        selectWindow("Neuropil");
        roiManager("Select",i);
        myName = Roi.getName;
        getStatistics(area, mean);
        if (mean<255){
            roiManager("Select",i);
            roiManager("Rename", "NP_"+myName);
            }
    }

    /*
        REFINE PARTICLES WITH LOCAL (ROI) VALUES?
    */

    //Draw ROI on Report
    selectWindow("Report");
    setForegroundColor(255,0,255);

    run("Line Width...", "line=3");
    for(i=0; i<roiManager("count"); i++){
        roiManager("Select",i);
        run("Draw", "slice");
    }
    setForegroundColor(0,255,255);
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    run("Draw", "stack");
    saveAs("Tiff", Path + "_report.tif");
    run("Close");

    //Create the result files (to be tested)
    selectWindow("Raw");
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");
    roiManager("Select", roiManager("count")-1);
    roiManager("Rename", "Neuropil");
    roiManager("Deselect");
    roiManager("Save", Path + "_RoiSet.zip");
    run("Set Measurements...", "area centroid display redirect=None decimal=3");
    roiManager("Measure");
    saveAs("Results", Path + "_Results.csv");
    run("Clear Results");


    //Close all non required images.
    selectWindow("Neuropil");
    run("Close");

    selectWindow("Raw");
    run("Close");

    selectWindow(T);
    run("Close");

    waitForUser("Analysis is over")

}//END MACRO
