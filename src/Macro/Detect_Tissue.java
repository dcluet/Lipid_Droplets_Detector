macro "Detect_Tissue"{

    //Detect Tissue in all slices using noise of the labelling

    //Arguments
    Argument = getArgument();
    Arguments = split(Argument, "*");

    myImage = Arguments[0];
    myPath = Arguments[1];

    //Set the drawing color to black
    setForegroundColor(0, 0, 0);

    //Select the current stack
    selectWindow(myImage);

    //Get width, height and number of slices
    W = getWidth();
    H = getHeight();
    N = nSlices;

    //Create a new empty recipient stack
    newImage("Brain-Shape", "8-bit white", W, H, N);

    //Process all slices
    for (S=1; S<=nSlices; S++){

        //Select the current stack
        selectWindow(myImage);

        //Get the initial number of ROI in the ROI manager
        nROI = roiManager("count");

        //Select the current slice
        setSlice(S);

        //Detect the shape of the tissue and add to the ROI manager
        makeRectangle(0,0,getWidth,getHeight);
        run("Gaussian Blur...", "sigma=20 slice");
        setAutoThreshold("Huang dark");
        run("Analyze Particles...", "size=100000-Infinity pixel show=Nothing add slice");

        //If more than one ROI is detected
        if (roiManager("count") > nROI+1){

            //Keep only the biggest new ROI
            Highlander(nROI);

            //Rename the ROI corresponding to the current shape of tissue
            roiManager("Select", nROI);
            roiManager("Rename", "Tissue slice "+S);

            //Draw this shape into the recipient stack at the correct slice
            selectWindow("Brain-Shape");
            setSlice(S);
            roiManager("Select", nROI);
            run("Fill", "slice");

        //If only one ROI is detected
        }else if(roiManager("count") == nROI+1){

            //Rename the ROI corresponding to the current shape of tissue
            roiManager("Select", nROI);
            roiManager("Rename", "Tissue slice "+S);

            //Draw this shape into the recipient stack at the correct slice
            selectWindow("Brain-Shape");
            setSlice(S);
            roiManager("Select", nROI);
            run("Fill", "slice");

        //If the tissue is not detected prompt the user
        }else if(roiManager("count") == nROI){

            waitForUser("PROBLEM:\nNO TISSUE DETECTED IN THIS SLICE");
        }

    }

    //Save ROIs
    ROIsave(myPath + "_Tissue_Slices.txt", "overwrite");

/*
================================================================================
*/

function ROIsave(path, option){

    //Save ROIs as a string txt file

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
================================================================================
*/

function Highlander(initial){

    //Keep the biggest ROI present in the ROI manager after a specific index

    //Analyze all ROI after a specific index
    for (r=initial; r<roiManager("count"); r++){

        //Initialize the boolean for deletion or not
        toDel = 0;

        //Select the current reference ROI
        roiManager("Select", r);

        //Measure the Area
        List.setMeasurements;
        Aref = List.getValue("Area");

        //Analyze all the remaining ROIs
        for (r2 = r+1; r2<roiManager("count"); r2++){

            //Select the current ROI to be compared
            roiManager("Select", r2);

            //Measure the Area
            List.setMeasurements;
            Atest = List.getValue("Area");

            //If the current ROI is smallest than the Reference ROI
            //the current ROI is deleted
            if (Atest<Aref){
                roiManager("Select", r2);
                roiManager("Delete");

                //The r2 index is corrected
                //to take into account the removal of 1 ROI
                r2 += -1;

            //If the current ROI is biggest than the Reference ROI
            //the reference ROI is deleted
            } else {
                toDel = 1;
            }
        }

        //Deletion of the Reference ROI if needed
        if (toDel==1){
            roiManager("Select", r);
            roiManager("Delete");

            //The r index is corrected
            //to take into account the removal of 1 ROI
            r += -1;
        }
    }
}//END HIGHLANDER

}//END Macro
