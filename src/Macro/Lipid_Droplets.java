macro "Lipid_Droplets"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

//Key parameters
seuil = 5;
Iterations = 5;
SizeMin = 20;
SizeMax = 2000;

//Parameters for Cleaning the false positive (but still remove them
//from the stack)
SizeMaxC = 1500;
CircMinC = 0.5;
CircMaxC = 1;
zDistance = 5;
enlargement = 5; //3 thus far

    //Close all non required images.
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");

    //Choose image file
    Path = File.openDialog("Choose file");

    //Command for Bioformat Importer
    CMD1 = "open=/";
    CMD1 += Path;
    CMD1 += " autoscale";
    CMD1 += " color_mode=Default";
    CMD1 += " rois_import=[ROI manager]";
    CMD1 += " view=Hyperstack stack_order=XYCZT";
    run("Bio-Formats Importer", CMD1);

    //Remove the non pixel unit
    CMD2 = "channels=1";
    CMD2 += " slices=" + nSlices();
    CMD2 += " frames=1";
    CMD2 += " unit=pixel";
    CMD2 += " pixel_width=1";
    CMD2 += " pixel_height=1";
    CMD2 += " voxel_depth=1.0000000";
    run("Properties...", CMD2);

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

    //Start BatchModecircularity=0.50-1.00
    selectWindow(T);
    setBatchMode("hide");
    selectWindow("Raw");
    setBatchMode("hide");
    selectWindow("ROI Manager");
    run("Close");
    setBatchMode(true);
    roiManager("reset");

    //Prepare Report
    selectWindow("Raw");
    makeRectangle(0,0,W,H);
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
            Twins_Killer("Raw",
                            nROI,
                            it,
                            seuil,
                            zDistance,
                            enlargement,
                            CircMinC,
                            SizeMaxC);

            //Update the total number of ROI validated
            nROI = roiManager("count");
        }
    }// End of Iterations

    newImage("Neuropil", "8-bit white", W, H, 1);
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

    //Create the result filesCircMinC, SizeMaxC
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
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);
    waitForUser("Analysis is over");

/*
================================================================================
*/

function Twins_Killer(myStack,
                        nROI,
                        it,
                        seuil,
                        zDistance,
                        enlargement,
                        CircMinC,
                        SizeMaxC){

    /*
        To increase speed I will sort the ROI by name
        The first 4 numbers are the slice.
        Thus at each iteration the program will assemble the ROI of each slice
        When the zDistance is to big I can break the twin_killer loop.

        PB: ImageJ as this format:
        slice 1: 0001
        slice 10: 00010
        ?????
        I have to rename myself firs and reorder
    */

    selectWindow(myStack);
    //Rename and sort all found ROIs
    Order_ROI(it);

    for (n =0; n< roiManager("count"); n++){
        setForegroundColor (0,0,0);
        roiManager("Select", n);
        Erase=0;
        List.setMeasurements;
        Xr = List.getValue("X");
        Yr = List.getValue("Y");
        Ar = List.getValue("Area");
        Cr = List.getValue("Circ.");
        Sr = getSliceNumber();

        if((Cr<CircMinC)||(Ar>SizeMaxC)){
            //Clean false positive
            Erase = 1;
        }else{
            for(N=n+1; N<roiManager("count"); N++){
                message = "Iteration " + it;
                message += " ROI " + n;
                message += " vs ROI " + N;
                message += " out of " + roiManager("count");
                message += " last score " + nROI;
                showStatus(message);
                roiManager("Select", N);
                List.setMeasurements;
                X = List.getValue("X");
                Y = List.getValue("Y");
                A = List.getValue("Area");
                C = List.getValue("Circ.");
                S = getSliceNumber();
                if((C<CircMinC)||(A>SizeMaxC)){
                    //Clean false positive
                    roiManager("Select", N);
                    run("Enlarge...", "enlarge=" + enlargement);
                    run("Fill", "slice");
                    roiManager("Delete");
                    N = N -1;
                }else{
                if ((abs(S-Sr)<=zDistance) && (abs(S-Sr)>0)){
                    d = sqrt( (X-Xr)*(X-Xr) + (Y-Yr)*(Y-Yr) );
                    if (d<seuil){
                        if(A>Ar){
                            Xr = X;
                            Yr = Y;
                            Ar = A;
                            Erase = 1;
                        }
                        if(A<Ar){
                            roiManager("Select", N);
                            run("Enlarge...", "enlarge=" + enlargement);
                            run("Fill", "slice");
                            roiManager("Delete");
                            N = N -1;
                        }
                    }
                }else if(abs(S-Sr)>zDistance){
                    /*
                        If more than z slices => No need to compare this
                        one the others -> Break
                    */
                    N = 10 * roiManager("count");
                }
            }
        }
        }
        if(Erase==1){
            roiManager("Select", n);
            setForegroundColor (0,0,0);
            run("Fill", "slice");
            roiManager("Delete");
            n=n-1;
            N=N-1;
         }
    }

    //Rename the ROI
    //To increase the speed fill only new particles
    selectWindow(myStack);
    setForegroundColor (0,0,0);
        for(i=0; i<roiManager("count"); i++){
            roiManager("Select",i);
            myName = Roi.getName;
            if(lastIndexOf(myName, "*")==-1){
                run("Enlarge...", "enlarge=" + enlargement);
                run("Fill", "slice");
                newName =myName + "*";
                roiManager("Rename", newName);
            }
        }
}//END TWIN_KILLER

/*
================================================================================
*/

function Order_ROI(it){

    for (roi=0; roi<roiManager("count"); roi++){

        //Only rename new ones
        roiManager("Select", roi);
        myName = Roi.getName;
        if(lastIndexOf(myName, "_")==-1){
            S = getSliceNumber();
            if (S>=10){
                Prefix = "";
            }else if(S<10){
                Prefix = "0";
            }

            PrefixRoi = "";
            if (roi<10000){
                PrefixRoi += "0";
            }
            if (roi<1000){
                PrefixRoi += "0";
            }
            if (roi<100){
                PrefixRoi += "0";
            }
            if (roi<10){
                PrefixRoi += "0";
            }

            newName = Prefix + S + "_" + it + "_" + PrefixRoi + roi;
            roiManager("Rename", newName);
        }
    }
    roiManager("sort");
}//END Order_ROI

}//END MACRO
