macro "Brain_Detection"{

//Get arguments
Argument = getArgument();
Arguments = split(Argument,"\t");

myImage = Arguments[0];
Path = Arguments[1];

selectWindow(myImage);

for (S=1; S<=nSlices; S++){
    nROI = roiManger("count");
    run("Gaussian Blur...", "sigma=5 slice");
    setAutoThreshold("Huang dark");
    run("Analyze Particles...", "size=100000-Infinity pixel show=Nothing add slice");
    if (roiManager("count") > nROI+1){

        Highlander(nROI);
        roiManager("Select", nROI+1);
        roiManager("Rename", "Brain slice "+S);
        waitForUser("Brain cleaned");

    }else if(roiManager("count") == nROI+1){

        roiManager("Select", nROI+1);
        roiManager("Rename", "Brain slice "+S);
        waitForUser("Brain detected");

    }else if(roiManager("count") == nROI){

        waitForUser("PROBLEM:\nNO BRAIN DETECTED IN THIS SLICE");
    }

}

//Save ROIs
ROIsave(Path + "_Brain_Slices.txt", "overwrite");

    /*


        Remove the hole
        select the ROIs
        run("Variance...", "radius=4 slice");
        run("Variance...", "radius=4");
        setAutoThreshold("Huang dark");
        run("Analyze Particles...", "size=10000-Infinity show=Masks add");

    */


/*
===============================================================================
                            FUNCTIONS
===============================================================================
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


}//END MACRO
