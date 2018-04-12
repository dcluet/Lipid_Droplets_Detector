macro "Main"{
    //INFOS
    tag = "v3.0.1"
    lastStableCommit = "247a2f70"
    gitlaburl = "http://gitlab.biologie.ens-lyon.fr/dcluet/Lipid_Droplets"

    //Welcome
    Welcome(tag, lastStableCommit, gitlaburl);

    /*
    ============================================================================
                                MAIN VARIABLES
    ============================================================================
    */

    //Extension
    myExt = ".czi";
    //Resolution of reference
    ResWref = 0.156;
    ResHref = 0.156;
    //Region of Analysis
    Selections = newArray("Whole tissue",  "Manual ROI");
    //xy threshold between 2 differents Lipid Droplets
    xythreshold = 5;
    xythresholdMicron = xythreshold * ResWref * ResHref;
    //z threshold between 2 different Lipid Droplets
    zthreshold = 5;
    //Initial Low resolution scan
    SizeMin = 7;
    SizeMinMicron = SizeMin * ResWref * ResHref;
    SizeMax = 15000;
    SizeMaxMicron = SizeMax * ResWref * ResHref;
    //False negative removal
    SizeMaxC = 500;
    SizeMaxCMicron = SizeMaxC * ResWref * ResHref;
    CircMinC = 0.5;
    CircMaxC = 1;

    //Number of iteration
    Iterations = 3;
    //Correction factor between iterations
    enlargement = 5;
    //Number of bins for the distributions graphs
    nBins = 50;

    /*
    ============================================================================
                                MAIN GUI
    ============================================================================
    */

    //Close all non required images.
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");

    //Initialisation of the Argument
    ARGcommon = "";

    //GUI
    Dialog.create("SETTINGS:");
    Dialog.addString("Extension of the stacks files: ", myExt, 5);
    Dialog.addMessage("Initial resolution used for calibration:");
    Dialog.addNumber("Pixel Width ", ResWref, 3, 5, "micron");
    Dialog.addNumber("Pixel Height: ", ResHref, 3, 5, "micron");
    Dialog.addChoice("Region to process: ", Selections, "Whole tissue");

    Dialog.addMessage("Thresholds between particles (microns):");
    Dialog.addNumber("XY Distance: ", xythresholdMicron, 3, 5, "microns");
    Dialog.addNumber("Z Distance: ", zthreshold, 3, 5, "slices");

    Dialog.addMessage("Parameters for the initial low-resolution scan:");
    Dialog.addNumber("Minimal surface: ", SizeMinMicron, 3, 7, "microns^2");
    Dialog.addNumber("Maximal surface: ", SizeMaxMicron, 3, 7, "microns^2");

    Dialog.addMessage("Parameters for the high-resolution scan:");
    Dialog.addNumber("Maximal surface: ", SizeMaxCMicron, 3, 7, "microns^2");
    Dialog.addNumber("Minimal circularity: ", CircMinC, 3, 5, "");
    Dialog.addNumber("Maximal circularity: ", CircMaxC, 3, 5, "");

    Dialog.addMessage("This program is based on iterative detection of the the brightest particles.");
    Dialog.addNumber("Number of maximal iterations: ", 3);
    Dialog.addNumber("Correction factor: ", 5, 0, 1, "pixels");
    Dialog.addNumber("Number of bins for the distributions: ", nBins, 0, 3, "");
    Dialog.show();

    myExt = Dialog.getString();
    ResWref = Dialog.getNumber();
    ResHref = Dialog.getNumber();
    resoRef = "" + ResWref + " x " + ResHref + " microns";
    ImResolution = ResWref*ResHref;

    myChoice = Dialog.getChoice();
    if (myChoice == "Whole tissue"){
        ARGcommon  += "Brain" + "*";
    }else if (myChoice == "Manual ROI"){
        ARGcommon  += "Manual ROI" + "*";
    }
    ARGcommon  += "" + ResWref + "*";
    ARGcommon  += "" + ResHref + "*";

    xythreshold = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + xythreshold + "*";
    zthreshold = Dialog.getNumber();
    ARGcommon  += "" + zthreshold + "*";

    SizeMin = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMin + "*";
    SizeMax = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMax + "*";

    SizeMaxC = Dialog.getNumber() / (ResWref * ResHref);
    ARGcommon  += "" + SizeMaxC + "*";
    CircMinC = Dialog.getNumber();
    ARGcommon  += "" + CircMinC + "*"; //Circ Min
    CircMaxC = Dialog.getNumber();
    ARGcommon  += "" + CircMaxC + "*"; //Circ Maximal
    Iterations = Dialog.getNumber();
    ARGcommon  += "" + Iterations + "*"; //Iterations
    enlargement = Dialog.getNumber();
    ARGcommon  += "" + enlargement + "*"; //Enlarge
    nBins = "" + Dialog.getNumber(); //Bins
    ARGcommon  += "" + nBins + "*"; //Bins

    /*
    ============================================================================
                            IDENTIFICATION OF THE FILES
    ============================================================================
    */

    //Retrieve folder to explore
    myTitle = "PLEASE CHOOSE THE FOLDER CONTAINING THE FILES TO PROCESS";
    PathFolderInput = getDirectory(myTitle);

    //Generate Finger Print
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    FP = "" + year + "-" + (month+1) + "-" + dayOfMonth + "_";
    FP += "" + hour + "-" + minute + "_";

    FPT = "" + year + "/" + (month+1) + "/" + dayOfMonth + " at ";
    FPT += "" + hour + ":" + minute;

    myAnalysis = PathFolderInput + FP + "_Files.txt";
    myCommands = PathFolderInput + FP + "_Parameters.txt";

    //Create text Files
    Listing = File.open(myAnalysis);
    File.close(Listing);
    Listing = File.open(myCommands);
    File.close(Listing);

    //Find all files and store path in myAnalysis
    listFiles(PathFolderInput, myExt, myAnalysis);

    //Retrieve number of files
    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");

    if (FileList.length>0){
        //Inform user
        DisplayInfo("<b>" + FileList.length + "</b> files have been found.<br>"
                    + "Press <b>OK</b> when ready for manual pre-processing.");
    }else{
        //Inform user
        DisplayInfo("<b>" + FileList.length + "</b> files have been found.<br>"
                    + "<b>The process will be aborted</b>.");
        de = File.delete(myAnalysis);
        de = File.delete(myCommands);
        exit();
    }

    //Prepare the markDown Report

    PathMD = getDirectory("macros");
    PathMD += "Droplets"+File.separator;
    PathMD += "Final_report.md";
    MD = File.openAsString(PathMD);

    MD = replace(MD, "MYFP", "" + FPT); //OK

    MD = replace(MD, "MYOS", getInfo("os.name"));
    MD = replace(MD, "MYJAVA", getInfo("java.version"));
    MD = replace(MD, "MYIJ", getVersion());

    MD = replace(MD, "MYSELECTION", myChoice);  //OK
    MD = replace(MD, "MYREFERENCE", "" + resoRef);  //OK
    MD = replace(MD, "XYTHRESHOLD", "" + (xythreshold* ImResolution) + " microns"); //OK
    MD = replace(MD, "ZTHRESHOLD", "" + (zthreshold* ImResolution) + " microns");   //OK
    MD = replace(MD, "MYITERATIONS", "" + Iterations);  //OK
    MD = replace(MD, "MYFACTOR", "" + enlargement + " pixels"); //OK

    MD = replace(MD, "MINSURF", "" + (SizeMin * ImResolution) + " microns");    //OK
    MD = replace(MD, "MAXSURF", "" + (SizeMax * ImResolution) + " microns");    //OK
    MD = replace(MD, "SURFMAXC", "" + (SizeMaxC * ImResolution) + " microns");  //OK
    MD = replace(MD, "MINCIRC", "" + CircMinC); //OK
    MD = replace(MD, "MAXCIRC", "" + CircMaxC); //OK

    File.saveString(MD, PathFolderInput + FP + "GLOBAL_REPORT.md");

    /*
    ============================================================================
                            LOOP OF PARAMETERS CREATION
    ============================================================================
    */

    //Set Freehand tool
    setTool("freehand");

    nFiles = FileList.length;
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
            run("Fire");
            run("RGB Color");

            selectWindow(Tissue);
            run("Enhance Contrast", "saturated=0.35");
            run("Red/Green");
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
        ARG += "" + FPT + "*" + FP;

        //Args = split(ARG, "*");
        //Array.show(Args);
        //waitForUser("");

        //Update the command file
        File.append(ARG, myCommands);

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

function listFiles(folder, extension, outFilePath) {

	list = getFileList(folder);
	for (i=0; i<list.length; i++) {
        if (File.isDirectory(folder+list[i])){
           	listFiles(""+folder+list[i], extension, outFilePath);
       	}

		if (endsWith(list[i], extension)){
            //Only file with a RFP twin are added
            File.append(""+folder+list[i], outFilePath);
		}
	}
}//END LISTFILES

/*
================================================================================
*/

function Welcome(myTag, myCommit, url){
    showMessage("WELCOME", "<html>"
			+"<font size=+3>"
			+"<h1><font color=rgb(77,172,174)>Lipid Droplets Analysis</h1>"
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
