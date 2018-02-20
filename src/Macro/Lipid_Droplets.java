macro "Lipid_Droplets"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

IJVersion = getVersion();

//Key parameters
seuil = 5;
nBins = 100;
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

    //Open the Markdown File
    PathMD = getDirectory("macros");
    PathMD += "Droplets"+File.separator;
    PathMD += "LayOut.md";
    MD = File.openAsString(PathMD);
    myCSV = "Name" + "\t" + "Area" + "\t" + "Corrected" + "\n";

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

    myimage = getTitle();
    getPixelSize(unit, pixelWidth, pixelHeight);
    reso = "" + pixelWidth + " " + unit + " x " + pixelHeight + " " + unit;

    MD = replace(MD, "MYIMAGE", myimage);
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    mydate = "" + year + "/" + (month+1) + "/" + dayOfMonth + " ";
    mydate += "" + hour + ":" + minute;
    MD = replace(MD, "MYDATE", mydate);
    MD = replace(MD, "MYOS", getInfo("os.name"));
    MD = replace(MD, "MYJAVA", getInfo("java.version"));
    MD = replace(MD, "MYIJ", IJVersion);
    MD = replace(MD, "MYRESOLUTION", reso);

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
    PathM1 = getDirectory("macros");
    PathM1 += "Droplets"+File.separator;
    PathM1 += "Stack_Editing.java";

    ARG1 = "Raw" + "\t";
    ARG1 += "" + Sstart + "\t";
    ARG1 += "" + Send + "\t";

    runMacro(PathM1, ARG1);

    //Duplicate the stack for Brain detection
    selectWindow("Raw");
    run("Duplicate...", "title=Brain duplicate");

    selectWindow("ROI Manager");
    run("Close");

    Detect_Brain("Brain", Path);

    //Create array of Brain areas. Start with 0 to have same index as slices
    ABrains = newArray();
    ABrains = Array.concat(ABrains, 0);

    for (S=1; S<=nSlices; S++){
        setSlice(S);
        ROIopen(Path + "_Brain_Slices.txt", S-1);
        getStatistics(area);
        ABrains = Array.concat(ABrains, area/1000000);
    }

    //Array.show(ABrains);

    selectWindow("Brain");
    close();
    roiManager("reset");

    //Draw Neuropil
    selectWindow("Raw");
    run("Enhance Contrast", "saturated=0.35");
    waitForUser("Draw the neuropil");
    getSelectionCoordinates(NeuroPilX, NeuroPilY);
    resetMinAndMax();

    //Start BatchMode

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

        /*
            INSERT A LOOP FOR DETECTION IN BRAIN WITHIN EACH SLICE
        */

        selectWindow("Raw");
        makeRectangle(0,0,W,H);
        run("Duplicate...", "title=Temp duplicate");
        makeRectangle(0,0,W,H);
        run("Convert to Mask", "method=MaxEntropy background=Dark calculate");

        for (S=1; S<=nSlices; S++){
            /*
                First slice is one but its corresponding ROI is in line 0 in
                the txt compression file
            */
            setSlice(S);
            ROIopen(Path + "_Brain_Slices.txt", S-1);

            run("Analyze Particles...", "size="+SizeMin+"-"+SizeMax+" add slice");
        }

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
    numberNP = 0;
    for(i=0; i<roiManager("count"); i++){
        selectWindow("Neuropil");
        roiManager("Select",i);
        myName = Roi.getName;
        getStatistics(area, mean);
        if (mean<255){
            roiManager("Select",i);
            roiManager("Rename", "NP_"+myName);
            numberNP += 1;
            }
    }
    MD = replace(MD, "MYDROPLETS", "" + roiManager("count"));
    MD = replace(MD, "MYNEUROPIL", "" + numberNP);

    /*
        REFINE PARTICLES WITH LOCAL (ROI) VALUES?
    */

    //Create Array of area values
    AValues = "";
    AValuesCorr = "";
    NValuesCorr = "";

    //Draw ROI on Report
    selectWindow("Report");

    run("Line Width...", "line=3");

    setForegroundColor(175,175,175);
    for (S=1; S<=nSlices; S++){
        /*
            First slice is one but its corresponding ROI is in line 0 in
            the txt compression file
        */
        setSlice(S);
        ROIopen(Path + "_Brain_Slices.txt", S-1);
        run("Draw", "slice");
    }

    setForegroundColor(255,0,255);
    for(i=0; i<roiManager("count"); i++){
        roiManager("Select",i);
        roiName=Roi.getName;
        List.setMeasurements;
        A = List.getValue("Area");
        ACorr = A/ABrains[getSliceNumber];
        myCSV += "" + roiName + "\t" + A + "\t" + ACorr + "\n";
        AValues += "" + A + "-";
        AValuesCorr += "" + ACorr + "-";
        if (lastIndexOf(roiName,"NP_") != -1){
            NValuesCorr += "" + ACorr + "-";
        }
        run("Draw", "slice");
    }
    setForegroundColor(0,255,255);
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    run("Draw", "stack");
    //saveAs("Tiff", Path + "_report.tif");

    if (lastIndexOf(IJVersion,"/") == -1){
        //ImageJ
        CMD = "save=" + Path + "_report.gif";
        run("Animated Gif... ",
            CMD);
    }else{
        //FIJI
        CMD = "name=Report ";
        CMD += "" + "set_global_lookup_table_options=[Do not use] ";
        CMD += "" + "optional=[] ";
        CMD += "" + "image=[No Disposal] ";
        CMD += "" + "set=500 ";
        CMD += "" + "number=-1 ";
        CMD += "" + "transparency=[No Transparency] ";
        CMD += "" + "red=0 green=0 blue=0 index=0 ";
        CMD += "" + "filename=" + Path + "_report.gif";
        run("Animated Gif ... ",
            CMD);
    }

    run("Close");
    MD = replace(MD, "MYGIF", myimage + "_report.gif");


    //Draw Distribution
    PathM2 = getDirectory("macros");
    PathM2 += "Droplets"+File.separator;
    PathM2 += "Distribution.java";

    /* INACTIVATED
    ARG2 = "" + SizeMin + "\t";
    ARG2 += "" + SizeMaxC + "\t";
    ARG2 += "" + nBins + "\t";
    ARG2 += "" + "Droplet size" + "\t";
    ARG2 += "" + Path + "\t";
    ARG2 += "" + "Raw_Values" + "\t";
    ARG2 += AValues;

    runMacro(PathM2, ARG2);
    */

    //Corrected Distribution (per million of pixel of brain surface);
    ARG2 = "" + 0 + "\t";
    ARG2 += "" + SizeMaxC/2 + "\t";
    ARG2 += "" + nBins + "\t";
    ARG2 += "" + "Droplet size per million pixels Brain" + "\t";
    ARG2 += "" + Path + "\t";
    ARG2 += "" + "_Corrected_Values_ALL" + "\t";
    ARG2 += AValuesCorr;


    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTJPG", myimage + "_Corrected_Values_ALL_Distribution.jpg");

    //Corrected Distribution for Neuropil
    ARG2 = "" + 0 + "\t";
    ARG2 += "" + SizeMaxC/2 + "\t";
    ARG2 += "" + nBins + "\t";
    ARG2 += "" + "Droplet size per million pixels Brain" + "\t";
    ARG2 += "" + Path + "\t";
    ARG2 += "" + "_Corrected_Values_NP" + "\t";
    ARG2 += NValuesCorr;


    runMacro(PathM2, ARG2);
    MD = replace(MD, "NPJPG", myimage + "_Corrected_Values_NP_Distribution.jpg");

    //Create the result filesCircMinC, SizeMaxC
    selectWindow("Raw");
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");
    roiManager("Select", roiManager("count")-1);
    roiManager("Rename", "Neuropil");
    roiManager("Deselect");
    roiManager("Save", Path + "_RoiSet.zip");

    //Save MD and CSV
    File.saveString(MD, Path + "_REPORT.md");
    File.saveString(myCSV, Path + "_data.csv");

    //Close all non required images.
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);
    waitForUser("Analysis is over");

/*
================================================================================
*/

function Highlander(initial){
    for (r=nROI; r<roiManager("count"); r++){
        toDel = 0;
        roiManager("Select", r);
        List.setMeasurements;
        Aref = List.getValue("Area");
        for (r2 = r+1; r2<roiManager("count"); r2++){
            roiManager("Select", r2);
            List.setMeasurements;
            Atest = List.getValue("Area");
            if (Atest<Aref){
                roiManager("Select", r2);
                roiManager("Delete");
                r2 += -1;
            } else {
                toDel = 1;
            }
        }

        if (toDel==1){
            roiManager("Select", r);
            roiManager("Delete");
            r += -1;
        }
    }
}//END HIGHLANDER

/*
===============================================================================
*/

function ROIsave(path, option){
    /*
        Recognized options:
        -"overwrite"
        -"append"

        Structure of the roi:
        "Name*X0;X1;...;Xn*Y0;Y1;...;Yn"
    */
    //create the file if not existing
    if(File.exists(path)==0){
        f = File.open(path);
        File.close(f);
    }

    //clear the file if overwriting is ordered
    if(option=="overwrite"){
        f = File.open(path);
        File.close(f);
    }

    //Loop of saving the ROIs
    for(roi=0; roi<roiManager("count"); roi++){
        roiManager("Select", roi);
        Roi.getCoordinates(xpoints, ypoints);
        Nom = Roi.getName();

        //Convert coordinates arrays as a single string
        X = "";
        Y = "";

        for(x=0; x<xpoints.length; x++){
            X += "" + xpoints[x];
            Y += "" + ypoints[x];

            if(x!=xpoints.length-1){
                X += ";";
                Y += ";";
            }
        }

        //Append the roi to the file
        File.append(Nom + "*" + X + "*" + Y,
                    path);
    }
}

/*
===============================================================================
*/

function Detect_Brain(myImage, myPath){

    selectWindow(myImage);

    for (S=1; S<=nSlices; S++){
        nROI = roiManager("count");
        setSlice(S);
        makeRectangle(0,0,getWidth,getHeight);
        run("Gaussian Blur...", "sigma=5 slice");
        setAutoThreshold("Huang dark");
        run("Analyze Particles...", "size=100000-Infinity pixel show=Nothing add slice");

        if (roiManager("count") > nROI+1){

            Highlander(nROI);
            roiManager("Select", nROI);
            roiManager("Rename", "Brain slice "+S);


        }else if(roiManager("count") == nROI+1){

            roiManager("Select", nROI);
            roiManager("Rename", "Brain slice "+S);


        }else if(roiManager("count") == nROI){

            waitForUser("PROBLEM:\nNO BRAIN DETECTED IN THIS SLICE");
        }

    }

    //Save ROIs
    ROIsave(myPath + "_Brain_Slices.txt", "overwrite");
}

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

/*
===============================================================================
*/

function ROIopen(path, index){
    /*
        index = -1 will open all ROI
        a specific number will open only the desired one but will not be added
        to the ROI manager
    */
    T = File.openAsString(path);

    //Separate the ROI
    ROI = split(T, "\n");

    if (index==-1){
        for(roi=0; roi<ROI.length; roi++){
            segments = split(ROI[roi], "*");
            Nom = segments[0];
            xpoints = split(segments[1], ";");
            ypoints = split(segments[2], ";");
            makeSelection("polygon", xpoints, ypoints);
            roiManager("Add");
            roiManager("Select", roiManager("count")-1);
            roiManager("Rename", Nom);
        }
    }else{
        segments = split(ROI[index], "*");
        Nom = segments[0];

        xpoints = split(segments[1], ";");
        ypoints = split(segments[2], ";");
        makeSelection("polygon", xpoints, ypoints);
    }
}

/*
===============================================================================
*/

}//END MACRO
