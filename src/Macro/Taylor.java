macro "Taylor"{

    //Retrieve the arguments within an array
    Argument = getArgument();
    Arguments = split(Argument, "\n");

    //Indication of reuse parameters
    myReuse = Arguments[0];

    //Path of the file containing the paths of the stacks
    myAnalysis = Arguments[1];

    //Path of the file containing the parameters of analysis
    myCommands = Arguments[2];

    //Zone(s) to analyze
    myChoice = Arguments[3];

    //Root folder of the Analysis
    PathFolderInput = Arguments[4];

    //Time Finger print for files
    FP = Arguments[5];

    //Time Finger print for reports
    FPT = Arguments[6];

    //Minimal number of particles to continue to next iteration
    minNew = parseFloat(Arguments[7]);

    //Channel to treat
    myChannel = Arguments[8];

    //Common parameters for all stacks
    ARGcommon = Arguments[9];

    /*
    ============================================================================
                        PATHS OF ACCESSORY MACROS
    ============================================================================
    */

    //Folder of the macro
    PM = getDirectory("macros");
    PM += "Droplets"+File.separator;

    //Crop the Stack
    PathM1 = PM + "Stack_Editing.java";

    //Close all non required images.
    PathM3 = PM + "Close_Images.java";

    /*
    ============================================================================
                                MAIN CODE
    ============================================================================
    */

    //Set Freehand tool
    setTool("freehand");

    //Extract the paths of the stacks
    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");
    nFiles = FileList.length;

    if (myReuse == "YES"){
        RecycleSetUp()
    }

    if (myReuse == "NO"){
        ClassicalSetUp();
    }

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function ClassicalSetUp(){

    //Generate taylored parameters for all stacks

    for (myFile=0; myFile<FileList.length; myFile++){

        setBatchMode(true);

        //Close all non required images.
        PathM3 = getDirectory("macros");
        PathM3 += "Droplets"+File.separator;
        PathM3 += "Close_Images.java";
        runMacro(PathM3);

        //Clean roiManager
        roiManager("reset");

        //Header CREATION
        myHeader = "File " + (myFile+1) + " out of " + FileList.length + ".";

        //Reinitiate ARG
        ARG = ARGcommon;

        //Select image file
        Path = FileList[myFile];

        //Command for Bioformat Importer
        CMD1 = "open=[";
        CMD1 += Path + "]";
        CMD1 += " autoscale";
        CMD1 += " color_mode=Default";
        CMD1 += " rois_import=[ROI manager]";
        CMD1 += " view=Hyperstack stack_order=XYCZT";
        run("Bio-Formats Importer", CMD1);

        Titre = getTitle;

        //Detecting Stacks
        if (Stack.isHyperstack==1){

            //Split channels
            run("Split Channels");

            //Attribute LUT to increase display resoltion
            Bodipy = "C1-" + Titre;
            Tissue = "C2-" + Titre;
            selectWindow(Bodipy);
            run("Enhance Contrast", "saturated=0.35");
            run("Green");
            run("RGB Color");

            selectWindow(Tissue);
            run("Enhance Contrast", "saturated=0.35");
            run("Red");
            run("RGB Color");

            //Create the display image and rename it as the original image
            imageCalculator("Add create stack", Bodipy, Tissue);
            rename(Titre);

            //Close intermediate stacks
            selectWindow(Bodipy);
            close();
            selectWindow(Tissue);
            close();
        }else{

            //Apply lut to classical stack
            run("Enhance Contrast", "saturated=0.35");
            run("Fire");

        }

        //Display the overlay
        selectWindow(Titre);
        setBatchMode("show");
        setBatchMode(false);

        //Prompt the user to indicate starting and ending slices
        waitForUser(myHeader +"\nSet on the starting slice");
        Sstart = getSliceNumber();
        waitForUser(myHeader +"\nSet on the ending slice");
        Send = getSliceNumber();

        //Crop the Stack
        ARG1 = Titre + "\t";
        ARG1 += "" + Sstart + "\t";
        ARG1 += "" + Send + "\t";

        //Update the paramaters of analysis of the current stack
        ARG += "" + Sstart + "*";
        ARG += "" + Send + "*";
        runMacro(PathM1, ARG1);

        //If a Manual ROI is necessary
        if (myChoice != "Whole tissue"){
            do{
                //Ask the user to draw the ROI
                setSlice(nSlices);
                waitForUser(myHeader +"\nDraw the Selection");
                getSelectionBounds(x, y, width, height);

                //Warning if no ROI is made
                if (x==0){
                    Warning = "WARNING!<br>";
                    Warning += "No Selection was drawn.";
                    DisplayInfo(Warning);
                }
            //Loop to ensure a ROI is done
            }while( (x==0) && (y==0));
        }else{
            makeRectangle(0,0,1,1);
        }

        //Get the coordinates of the Manual ROI
        getSelectionCoordinates(NeuroPilX, NeuroPilY);

        //Convert the arrays of X and Y coordinates into string arguments
        NPX = "";
        NPY = "";
        for(i=0; i<NeuroPilX.length; i++){
            NPX += "" + NeuroPilX[i] + "-";
            NPY += "" + NeuroPilY[i] + "-";
        }

        //Update the paramaters of analysis of the current stack
        ARG += NPX + "*";
        ARG += NPY + "*";
        ARG += Path + "*";
        ARG += PathFolderInput + "*";
        ARG += "" + (myFile/nFiles) + "*";
        ARG += "" + FPT + "*" + FP + "*";
        ARG += "" + minNew + "*";
        ARG += "" + myChannel;

        //Update the command file
        File.append(ARG, myCommands);
    }


    //Close all non required images.
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");
}//END CLASSICALSETUP

/*
================================================================================
*/

function RecycleSetUp(){

    //Combine current GUI parameters with old taylored parameters

    OK = 0;
    do{
        //Prompt the user to specify a parameter.txt file
        existingSet = File.openDialog("Please indicate which file to use as reference");

        //Check the validity of the file
        if (endsWith(existingSet, "_Parameters.txt") == 0){
            Warning = "WARNING!<br>";
            Warning += "The file is not correct.<br>";
            Warning += "Should ends with _Parameters.txt";
            DisplayInfo(Warning);
        }else{
            OK = 1;
        }
    //Loop to ensure a correct file is given
    }while(OK == 0);

    //Extract the old parameters
    listCommands = File.openAsString(existingSet);
    myCommandsList = split(listCommands, "\n");

    //For all files concatenate "new" global parameter with extracted
    //old taylored parameters
    for (c=0; c<myCommandsList.length; c++){
        //Use the correct concatenated arguments
        Arguments = split(myCommandsList[c], "*");

        Sstart = parseFloat(Arguments[14]);
        Send = parseFloat(Arguments[15]);
        NeuroPilXtext = Arguments[16];
        NeuroPilYtext = Arguments[17];
        Path = Arguments[18];
        myRoot = Arguments[19];
        myProgress = parseFloat(Arguments[20]);

        //Generate the paramaters of analysis of the current stack
        ARG = ARGcommon;
        ARG += "" + Sstart + "*";
        ARG += "" + Send + "*";
        ARG += NeuroPilXtext + "*";
        ARG += NeuroPilYtext + "*";
        ARG += Path + "*";
        ARG += myRoot + "*";
        ARG += "" + myProgress + "*";
        ARG += "" + FPT + "*" + FP + "*";
        ARG += "" + minNew + "*";
        ARG += "" + myChannel;

        //Update the command file
        File.append(ARG, myCommands);

    }

    //Close all non required images.
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");
}

/*
================================================================================
*/

function DisplayInfo(Message){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Automated Detection of labeled particles <br> in microscopic stacks of Drosophila M. tissues</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}
