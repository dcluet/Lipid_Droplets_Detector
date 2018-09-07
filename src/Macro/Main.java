macro "Main"{
    //INFOS
    tag = "v4.1.0"
    lastStableCommit = "09c54a63"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    /*
    ============================================================================
                        PATHS OF ACCESSORY MACROS
    ============================================================================
    */

    //Clear all Images
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";

    //Launch Choice of Analysis
    PathGetParam = getDirectory("macros");
    PathGetParam += "Droplets"+File.separator;
    PathGetParam += "Get_Parameters.java";

    //Launch GUI
    PathGUI = getDirectory("macros");
    PathGUI += "Droplets"+File.separator;
    PathGUI += "Main_GUI.java";

    //Launch Files identification
    PathIdent = getDirectory("macros");
    PathIdent += "Droplets"+File.separator;
    PathIdent += "Identify_Files.java";

    //Select Channel
    PathSelChannel = getDirectory("macros");
    PathSelChannel += "Droplets"+File.separator;
    PathSelChannel += "Select_Channel.java";



    /*
    ============================================================================
                                CLEAN IMAGEJ
    ============================================================================
    */

    //Close all non required images.
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");

    /*
    ============================================================================
                                WELCOME WINDOW
    ============================================================================
    */

    //Welcome
    Welcome(tag, lastStableCommit, gitlaburl);

    /*
    ============================================================================
                        SELECT ANALYSIS TYPE AND GET SETTINGS
    ============================================================================
    */

    param = runMacro(PathGetParam);

    /*
    ============================================================================
                        MAIN GUI AND FOLDER IDENTIFICATION
    ============================================================================
    */

    ResultGUI = runMacro(PathGUI, param);

    //Extract key parameters and command line
    myParameters = split(ResultGUI, "\n");
    myReuse = myParameters[0];
    myExt = myParameters[1];
    ARGcommon = myParameters[2];
    FP = myParameters[3];
    FPT = myParameters[4];
    PathFolderInput = myParameters[5];
    myChoice = myParameters[6];
    minNew = parseFloat(myParameters[7]);
    nBins = parseFloat(myParameters[8]);

    /*
    ============================================================================
                            IDENTIFICATION OF THE FILES
    ============================================================================
    */

    myAnalysis = PathFolderInput + FP + "_Files.txt";
    myCommands = PathFolderInput + FP + "_Parameters.txt";

    //Create text Files
    Listing = File.open(myAnalysis);
    File.close(Listing);
    Listing = File.open(myCommands);
    File.close(Listing);

    //Launch Files Identification
    ArgFiles = myExt + "*";
    ArgFiles += PathFolderInput + "*";
    ArgFiles += myAnalysis + "*";
    ArgFiles += myCommands + "*";
    ArgFiles += myReuse;

    runMacro(PathIdent, ArgFiles);

    /*
    ============================================================================
                                SELECT THE CHANNEL
    ============================================================================
    */

    //Launch Channel Selection
    ArgChannel = myAnalysis + "*";
    ArgChannel += myReuse;
    myChannel = runMacro(PathSelChannel, ArgChannel);

    /*
    ============================================================================
                            LOOP OF PARAMETERS CREATION
    ============================================================================
    */

    //Set Freehand tool
    setTool("freehand");

    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");
    nFiles = FileList.length;
    
    if (myReuse == "YES"){
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
        PathM3 = getDirectory("macros");
        PathM3 += "Droplets"+File.separator;
        PathM3 += "Close_Images.java";
        runMacro(PathM3);

        //Clean roiManager
        roiManager("reset");

    }


    if (myReuse == "NO"){
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
            PathM1 = getDirectory("macros");
            PathM1 += "Droplets"+File.separator;
            PathM1 += "Stack_Editing.java";

            ARG1 = Titre + "\t";
            ARG1 += "" + Sstart + "\t";
            ARG1 += "" + Send + "\t";

            ARG += "" + Sstart + "*";
            ARG += "" + Send + "*";
            runMacro(PathM1, ARG1);

            if (myChoice != "Whole tissue"){
                do{
                    setSlice(nSlices);
                    waitForUser(myHeader +"\nDraw the neuropil");
                    getSelectionBounds(x, y, width, height);
                    if (x==0){
                        Warning = "WARNING!<br>";
                        Warning += "No Neuropil was drawn.";
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
        PathM3 = getDirectory("macros");
        PathM3 += "Droplets"+File.separator;
        PathM3 += "Close_Images.java";
        runMacro(PathM3);

        //Clean roiManager
        roiManager("reset");

    }

    //Inform user
    DisplayInfo("Press <b>OK</b> when ready for automated analysis.");


    /*
    ============================================================================
                            LOOP OF ANALYSIS
    ============================================================================
    */

    //Get Beginning Time
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    T1 = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    T1 += "" + hour + ":" + minute;

    //Retrieve Commands
    Commands = File.openAsString(myCommands);
    CommandsList = split(Commands, "\n");

    for (c=0; c<CommandsList.length; c++){
        //Use the correct concatenated arguments
        ARG = CommandsList[c];

        //Run Lipid_Droplets
        PathLD = getDirectory("macros");
        PathLD += "Droplets"+File.separator;
        PathLD += "Lipid_Droplets.java";
        setBatchMode(true);
        runMacro(PathLD, ARG);

    }

    /*
    ============================================================================
                            STATISTICAL ANALYSIS
    ============================================================================
    */



    //Run stats analysis
    PathMS = getDirectory("macros");
    PathMS += "Droplets"+File.separator;
    PathMS += "Stats.java";

    ARGMS = PathFolderInput + "*";
    ARGMS += myAnalysis + "*";
    ARGMS += FP + "*";
    ARGMS += "" + nBins + "*";

    runMacro(PathMS, ARGMS);

    //Get Ending Time
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    T2 = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    T2 += "" + hour + ":" + minute;

    EndProcess(T1,T2);

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function Welcome(myTag, myCommit, url){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets and REPO Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
			+"<ul>"
			+"<li>Version: " + myTag + "</li>"
			+"<li>Last stable commit: " + myCommit + "</li>"
			+"</ul>"
			+"<p><font color=rgb(100,100,100)>Cluet David<br>"
            +"Research Ingeneer,PHD<br>"
            +"<font color=rgb(77,172,174)>CNRS, ENS-Lyon, LBMC</p>"
			);
}//END WELCOME

/*
================================================================================
*/

function EndProcess(Time1, Time2){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>Analysis is over.</p>"
			+"<ul>"
			+"<li>Beginning: " + Time1 + "</li>"
			+"<li>End: " + Time2 + "</li>"
			+"</ul>"
			);
}//END ENDPROCESS

/*
================================================================================
*/

function DisplayInfo(Message){
    showMessage("", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
			+"<font size=+0>"
			+"<font color=rgb(0,0,0)>"
            +"<p>" + Message + "</p>"
			);
}//END DisplayInfo

}//END MACRO
