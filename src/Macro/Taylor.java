macro "Taylor"{

    Argument = getArgument();
    Arguments = split(Argument, "\n");
    myReuse = Arguments[0];
    myAnalysis = Arguments[1]
    myCommands = Arguments[2];
    myChoice = Arguments[3];
    PathFolderInput = Arguments[4];
    FP = Arguments[5];
    FPT = Arguments[6];
    minNew = parseFloat(Arguments[7]);
    myChannel = Arguments[8];
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

        selectWindow(Titre);
        setBatchMode("show");
        setBatchMode(false);

        waitForUser(myHeader +"\nSet on the starting slice");
        Sstart = getSliceNumber();
        waitForUser(myHeader +"\nSet on the ending slice");
        Send = getSliceNumber();

        //Crop the Stack
        ARG1 = Titre + "\t";
        ARG1 += "" + Sstart + "\t";
        ARG1 += "" + Send + "\t";

        ARG += "" + Sstart + "*";
        ARG += "" + Send + "*";
        runMacro(PathM1, ARG1);

        if (myChoice != "Whole tissue"){
            do{
                setSlice(nSlices);
                waitForUser(myHeader +"\nDraw the Selection");
                getSelectionBounds(x, y, width, height);
                if (x==0){
                    Warning = "WARNING!<br>";
                    Warning += "No Selection was drawn.";
                    DisplayInfo(Warning);
                }
            }while( (x==0) && (y==0));
        }else{
            makeRectangle(0,0,1,1);
        }

        getSelectionCoordinates(NeuroPilX, NeuroPilY);


        NPX = "";
        NPY = "";
        for(i=0; i<NeuroPilX.length; i++){
            NPX += "" + NeuroPilX[i] + "-";
            NPY += "" + NeuroPilY[i] + "-";
        }
        ARG += NPX + "*";
        ARG += NPY + "*";

        ARG += Path + "*";
        ARG += PathFolderInput + "*";
        ARG += "" + (myFile/nFiles) + "*";
        ARG += "" + FPT + "*" + FP + "*";
        ARG += "" + minNew + "*";
        ARG += "" + myChannel;

        //Args = split(ARG, "*");
        //Array.show(Args);
        //waitForUser("");

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
    OK = 0;
    do{
        existingSet = File.openDialog("Please indicate which file to use as reference");
        if (endsWith(existingSet, "_Parameters.txt") == 0){
            Warning = "WARNING!<br>";
            Warning += "The file is not correct.<br>";
            Warning += "Should ends with _Parameters.txt";
            DisplayInfo(Warning);
        }else{
            OK = 1;
        }
    }while(OK == 0);

    listCommands = File.openAsString(existingSet);
    myCommandsList = split(listCommands, "\n");

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
