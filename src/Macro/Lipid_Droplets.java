macro "Lipid_Droplets"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

//Get the version of IJ for the MarkDown Report file
IJVersion = getVersion();

//Arguments
Argument = getArgument();
Arguments = split(Argument, "*");

//Selection zone
Selection = Arguments[0];

//Resolution of the reference stack
//Pixel Width in microns
ResWref = parseFloat(Arguments[1]);
//Pixel Height in microns
ResHref = parseFloat(Arguments[2]);

//Minimal distance between 2 particles centers to be considered as separated
seuil = parseFloat(Arguments[3]);

//Minimal number of slices to consider 2 particles as separated
zDistance = parseFloat(Arguments[4]);

//Minimal area of the particles of interest
SizeMin = parseFloat(Arguments[5]);

//maximal size to take into account potential big bright
//false positive signals
//Infinity can be a wrong choice -> detect the whole tissue
SizeMax = parseFloat(Arguments[6]);

//Maximal area of the particles of interest
SizeMaxC = parseFloat(Arguments[7]);

//Minimal circularity of the particles of interest
CircMinC = parseFloat(Arguments[8]);

//Maximal circularity of the particles of interest
CircMaxC = parseFloat(Arguments[9]);

//Number of iterations
Iterations = parseFloat(Arguments[10]);

//Distance of enlargement (in all directions) when removing the particles from
//the stack
enlargement = parseFloat(Arguments[11]);

//Number of bins for the distributions
nBins = parseFloat(Arguments[12]);

//Precise if the Maximum... treatment is applied
enhance = parseFloat(Arguments[13]);

//Path of the file contaiing the path of file containing the path of the files
myAnalysis = Arguments[14];

//Starting slice
Sstart = parseFloat(Arguments[15]);

//Ending slice
Send = parseFloat(Arguments[16]);

//Coordinates of the manual selection
//X coordinates
NeuroPilXtext = Arguments[17];
NeuroPilX = split(NeuroPilXtext, "-");

//Y coordinates
NeuroPilYtext = Arguments[18];
NeuroPilY = split(NeuroPilYtext, "-");

//Path of the current stack
Path = Arguments[19];

//Path of the root folder of the analysis
myRoot = Arguments[20];

//Position of the stack in the listing -> for Progressbar
myProgress = parseFloat(Arguments[21]);

//Time finger print to be included in reports
FPT = Arguments[22];

//Time finger print for file names
FP = Arguments[23];

//Minimal number of particles to continue to next iteration
minimumFound = parseFloat(Arguments[24]);

//Channel to be treated
channels = split(Arguments[25], ";");
channel = channels[0];

/*
============================================================================
                    PATHS OF ACCESSORY MACROS
============================================================================
*/

//Crop the Stack
PathCS = getDirectory("macros");
PathCS += "Droplets"+File.separator;
PathCS += "Stack_Editing.java";

//Draw Distribution
PathDD = getDirectory("macros");
PathDD += "Droplets"+File.separator;
PathDD += "Distribution.java";

//Close all non required images.
PathClear = getDirectory("macros");
PathClear += "Droplets"+File.separator;
PathClear += "Close_Images.java";

//Remove Twins Particles
PathTK = getDirectory("macros");
PathTK += "Droplets"+File.separator;
PathTK += "Twins_Killer.java";

//Detect Tissue
PathDT = getDirectory("macros");
PathDT += "Droplets"+File.separator;
PathDT += "Detect_Tissue.java";

//Remove UnWanted Particles
PathRUW = getDirectory("macros");
PathRUW += "Droplets"+File.separator;
PathRUW += "RemoveUnWanted.java";

/*
===============================================================================
                            CORE PROGRAM
===============================================================================
*/

    //Close all non required images.
    runMacro(PathClear);

    //Clean roiManager
    roiManager("reset");

    /*
    ============================================================================
                OPEN THE CURRENT FILE, GET PARAMETERS AND CREATE FILES
    ============================================================================
    */

    //Create the Outputfolder
    Parent = File.getParent(Path) + File.separator;
    NameFile = File.getName(Path);
    NameFile = substring(NameFile,
                            0,
                            lastIndexOf(NameFile, ".")
                            );
    FolderOutput = Parent + FP + "_" + NameFile + File.separator;

    //Hardcoded fileseparator for markdown reader.
    FolderOutputRelative = FP + "_" + NameFile + "/";

    //Create the folder if it doesn t exist
    if (File.exists(FolderOutput)!=1){
        File.makeDirectory(FolderOutput);
    }

    //Open the Markdown File
    PathMD = getDirectory("macros");
    PathMD += "Droplets"+File.separator;
    PathMD += "LayOut.md";
    MD = File.openAsString(PathMD);

    //Initialize the string for the csv file
    myCSV = "Name" + "\t" + "Slice" + "\t" + "X" + "\t" + "Y"
            + "\t" + "Area um2" + "\t" + "Corrected um2"
            + "\t" + "Mean Intensity" + "\n";

    //Command for Bioformat Importer to open the image
    CMD1 = "open=[";
    CMD1 += Path + "]";
    CMD1 += " autoscale";
    CMD1 += " color_mode=Default";
    CMD1 += " rois_import=[ROI manager]";
    CMD1 += " view=Hyperstack stack_order=XYCZT";

    run("Bio-Formats Importer", CMD1);

    //Obtain the stack's name
    myimage = getTitle();

    //Retrieve stack's resolution
    getPixelSize(unit, pixelWidth, pixelHeight);

    //Prepare strings for report
    reso = "" + pixelWidth + " " + unit + " x " + pixelHeight + " " + unit;
    resoRef = "" + ResWref + " " + unit + " x " + ResHref + " " + unit;

    //Detecting Stacks
    if (Stack.isHyperstack==1){

        //Split channels
        run("Split Channels");

        //Keep only the first channel to perform analysis
        selectWindow(channel + myimage);

        //Rename the channel of interest as the original image
        //-> the program is then common for mono and multi channel stacks
        rename(myimage);
    }

    //Crop the Stack
    selectWindow(myimage);
    ARG1 = myimage + "\t";
    ARG1 += "" + Sstart + "\t";
    ARG1 += "" + Send + "\t";

    runMacro(PathCS, ARG1);

    //Recalibrating the area values depending on resoltion.
    RefResolution = ResWref*ResHref;
    ImResolution = pixelWidth*pixelHeight;
    Ratio = RefResolution/ImResolution;
    SizeMin = SizeMin * Ratio;
    SizeMax = SizeMax * Ratio;
    SizeMaxC = SizeMaxC * Ratio;

    //Get timing for report
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
    run("Duplicate...", "title=Intensity duplicate");
    run("Duplicate...", "title=Brain duplicate");
    selectWindow("Raw");

    //Get the number of slices
    myslices = nSlices;

    /*
    ============================================================================
            DETECT AND ANALYZE THE SHAPES OF THE TISSUE WITHIN THE STACK
    ============================================================================
    */

    //Ensure no ROI are present in the ROI manager
    roiManager("reset");

    //Detect tissue in all slices
    ARGT = "Brain" + "*";
    ARGT += FolderOutput + NameFile;
    runMacro(PathDT, ARGT);

    //Create array of tissue areas. Start with 0 to have same index as slices
    ABrains = newArray();
    ABrains = Array.concat(ABrains, 0);

    //Array number of ROI for each slice
    LDperSlice = newArray(nSlices);

    //Array number of ROI in Manual ROI for each slice
    LDNPperSlice = newArray(nSlices);

    //Array total surface of ROI for each slice
    AreaLDperSlice = newArray(nSlices);

    //Array total surface of ROI in Manual ROI for each slice
    AreaLDNPperSlice = newArray(nSlices);

    //Initialize the string variable for the MD report
    mybrains = "";

    //Initialize the total surface of the Brain (all slices)
    TotalBrainSurface = 0;

    //Calculate the total tissue surface within the stack
    for (S=1; S<=nSlices; S++){

        //Select the current slice
        setSlice(S);

        //Open the corresponding tissue ROI
        ROIopen(FolderOutput + NameFile + "_Tissue_Slices.txt", S-1);

        //Get the surface (pixel) of the shape
        getStatistics(area);

        //Append the coorected value (microns) into the tissue area array.
        ABrains = Array.concat(ABrains, area * pixelWidth * pixelHeight/1000000);

        //Update the total surface value (microns)
        TotalBrainSurface += area * pixelWidth * pixelHeight/1000000;

        //Update the report string variable
        mybrains += "Tissue slice " + S + " : **" + ABrains[S] + "**\n\n";
    }

    //Get statistics on tissue shapes area
    Array.getStatistics(ABrains, min, max, meanTissue, stdDev);

    //Close the Tissue window (no more needed)
    selectWindow("Brain");
    close();

    //Remove all ROI from ROI manager
    roiManager("reset");

    /*
    ============================================================================
                        PREPARE ANALYSIS WITH MANUAL ROI
    ============================================================================
    */

    //Add the Manual ROI to the ROI manager
    selectWindow("Raw");
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");

    //Get correct Area (microns) and coordinates (as arrays)
    roiManager("Select", 0);
    List.setMeasurements;
    ANP = List.getValue("Area") * pixelWidth * pixelHeight;
    XNP = List.getValue("X");
    YNP = List.getValue("Y");

    //Correct the area using the mean area of the tissue
    ACorr = ANP/meanTissue;

    //Update CSV
    myCSV += "" + "Neuropil" + "\t" + nSlices + "\t";
    myCSV +=  "" + XNP + "\t" + YNP + "\t" + ANP + "\t" + ACorr + "\n";

    //Prepare a new image with the shape of the Manual ROI
    newImage("Neuropil", "8-bit white", W, H, 1);
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    setForegroundColor (0,0,0);
    run("Fill");

    //Clean ROI Manager
    roiManager("reset");

    //Prepare Report Stack
    selectWindow("Raw");
    makeRectangle(0,0,W,H);
    run("Duplicate...", "title=Report duplicate");
    run("Enhance Contrast", "saturated=0.35");
    run("RGB Color");

    /*
    ============================================================================
                        PREPARE IMAGE TO DETECT PARTICLES
    ============================================================================
    */

    //Select the window in which the detection will be perform
    selectWindow("Raw");

    //Ensure we use the whole image
    makeRectangle(0,0,W,H);

    //Use Gaussian Blur to "solidify" the pixelized particles
    run("Gaussian Blur...", "sigma=1 stack");
    if (enhance==1){
        //Enhance signal
        run("Maximum...", "radius=5 stack");
    }

    /*
    ============================================================================
                            ITERATIVE ANALYSIS
    ============================================================================
    */

    //Reference number of particles
    nROI = 0;

    //Iteratively detect the strongest particles and remove them
    for (it=1; it<=Iterations; it++){

        selectWindow("Raw");

        //Process all slices
        for (S=1; S<=nSlices; S++){

            //Select the current slice
            setSlice(S);

            //Select the whole picture
            makeRectangle(0,0,W,H);

            //Threshold the image to detect the brightest particles
            setAutoThreshold("MaxEntropy dark");
            run("Analyze Particles...", "size="+ 1 +"-"+SizeMax+" add slice");
        }

        //Determine the number of detected particles
        myFound = roiManager("count");

        //If new particles have been found
        if (nROI<myFound){

            //Determine which image to use as reference to
            //Remove all unwanted particles
            if (Selection != "Manual ROI"){

                //if the user study the global tissue +/- a Manual ROI
                myWindow = "Brain-Shape";

            }else{

                //if the user study only a Manual ROI
                myWindow = "Neuropil";
            }
            //Remove all unwanted particles
            ARGRUW = myWindow + "*" + enlargement;
            runMacro(PathRUW, ARGRUW);

            //Remove all twins particles
            ARG5 = "Raw" + "*";
            ARG5 += "" + nROI + "*";
            ARG5 += "" + it + "*";
            ARG5 += "" + seuil + "*";
            ARG5 += "" + zDistance + "*";
            ARG5 += "" + enlargement + "*";
            ARG5 += "" + CircMinC + "*";
            ARG5 += "" + SizeMaxC + "*";
            ARG5 += "" + SizeMin + "*";
            ARG5 += "" + CircMaxC + "*";
            ARG5 += "" + enhance + "*";
            ARG5 += FPT + "*";
            ARG5 += "" + myProgress;

            runMacro(PathTK, ARG5);

            //Check if it is worse to continue
            if (nROI+minimumFound>roiManager("count")){
                it = 10 * Iterations;
            }

            //Update the total number of ROI validated
            nROI = roiManager("count");
        }
    }// End of Iterations


    /*
    ============================================================================
                        IDENTIFY PARTICLES IN THE MANUAL ROI
    ============================================================================
    */

    //Initialisation of arrays containing number of particles and total surface
    for(np = 0; np<LDperSlice.length; np++){
        LDperSlice[np] = 0;
        AreaLDperSlice[np] = 0;
        LDNPperSlice[np] = 0;
        AreaLDNPperSlice[np] = 0;
    }

    //Number of particles within the Tissue but OUTSIDE of the MANUAL ROI
    numberNP = 0;

    //Process all ROIs in the ROI Manager
    for(i=0; i<roiManager("count"); i++){

        //Select the window used for the detection
        selectWindow("Raw");

        //SElect current ROI
        roiManager("Select",i);

        //Correct the slice number (first index = 1 -> 0)
        currentSlice = getSliceNumber - 1;

        //Measure the particle
        List.setMeasurements;
        //Get the corrected (microns) area
        ALD = List.getValue("Area") * pixelWidth * pixelHeight;

        //Update the number of particle for the current slice
        LDperSlice[currentSlice] = LDperSlice[currentSlice] + 1;

        //Update the total area of particle for the slice
        AreaLDperSlice[currentSlice] = AreaLDperSlice[currentSlice] + ALD;

        //Select the window with the shape of the Manual Roi
        selectWindow("Neuropil");

        //Re-select the current ROI
        roiManager("Select",i);

        //Get the name and statistics
        myName = Roi.getName;
        getStatistics(area, mean);

        //If the particle is at least partially in the Manual ROI
        if (mean<255){

            //Select the current ROI
            roiManager("Select",i);

            //Rename the particle by adding the prefix NP
            roiManager("Rename", "NP_"+myName);

            //Update the total number of particles in the Manual ROI
            numberNP += 1;

            //Update the number of particle in the Manual ROI per slice
            LDNPperSlice[currentSlice] = LDNPperSlice[currentSlice] + 1;

            //Update the total surface of particle in the Manual ROI per slice
            AreaLDNPperSlice[currentSlice] = AreaLDNPperSlice[currentSlice] + ALD;
            }
    }

    //Total number of particles
    totalLD = roiManager("count");

    /*
        REFINE PARTICLES WITH LOCAL (ROI) VALUES?
    */

    //Create Array of area values
    AValues = "";
    NValues = "";
    EValues = "";

    AValuesCorr = "";
    NValuesCorr = "";
    EValuesCorr = "";

    IValues = "";
    INValues = "";
    IEValues = "";

    /*
    ============================================================================
                    CREATE THE REPORT STACK AND MEASURE INTENSITY
    ============================================================================
    */

    //Draw the shape of the Tissue on Report stack
    selectWindow("Report");

    //Set linewidth to 3
    run("Line Width...", "line=3");

    //Set drawing color to grey
    setForegroundColor(175,175,175);

    for (S=1; S<=nSlices; S++){
        /*
            First slice is one but its corresponding ROI is in line 0 in
            the txt compression file
        */
        setSlice(S);

        //Open the shape of the tissue for the current slice
        ROIopen(FolderOutput + NameFile + "_Tissue_Slices.txt", S-1);

        //Draw the shape
        run("Draw", "slice");
    }

    //Set the drawing color to magenta
    setForegroundColor(255,0,255);

    //Process all ROIs
    for(i=0; i<roiManager("count"); i++){

        //Select the window to measure raw intensity
        selectWindow("Intensity");

        //select the current ROI
        roiManager("Select",i);

        //Get name of the ROI
        roiName=Roi.getName;

        //Get Measures
        List.setMeasurements;

        //Corrected (microns) area and the (X,Y) position of the center
        A = List.getValue("Area") * pixelWidth * pixelHeight;
        X = List.getValue("X");
        Y = List.getValue("Y");

        //Get the intensity
        Intensity = List.getValue("Mean");

        //Update the CSV
        myCSV += "" + roiName + "\t" + getSliceNumber + "\t";
        myCSV += "" + X + "\t" + Y + "\t" + A + "\t" + ACorr + "\t";
        myCSV += "" + Intensity + "\n";

        //Update the variables containing the values for ALL particles
        AValues += "" + A + "-";
        AValuesCorr += "" + ACorr + "-";
        IValues += "" + Intensity + "-";

        //Update the variables containing the values for Manual ROI particles
        if (lastIndexOf(roiName,"NP_") != -1){
            //Neuropil only
            NValues += "" + A + "-";
            NValuesCorr += "" + ACorr + "-";
            INValues += "" + Intensity + "-";
        }else{
            //Update the variables containing the values for
            //Non Manual ROI particles
            EValues += "" + A + "-";
            EValuesCorr += "" + ACorr + "-";
            IEValues += "" + Intensity + "-";
        }

        //Select the report Stack
        selectWindow("Report");

        //Select the current ROI
        roiManager("Select",i);

        //Draw the particle
        run("Draw", "slice");
    }

    //Set drawing color to cyan
    setForegroundColor(0,255,255);

    //Select the report stack
    selectWindow("Report");

    //Draw the manual ROI
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");
    roiManager("Select", roiManager("count")-1);
    roiManager("Rename", "Neuropil");
    run("Draw", "stack");

    makeRectangle(0, 0, W, H);

    //Save Gif HD
    if (lastIndexOf(IJVersion,"/") == -1){
        //ImageJ
        CMD = "save=[" + FolderOutput + NameFile + "_report_HD.gif]";

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
        CMD += "" + "filename=[" + FolderOutput + NameFile + "_report_HD.gif]";

        run("Animated Gif ... ",
            CMD);
    }

    makeRectangle(0, 0, W, H);

    //Reduce size
    run("Scale...",
        "x=- y=- z=1.0 width=1000 height=1000 depth=" + myslices
        + " interpolation=None average process create");

    //Get the number of slices
    localSliceNumber = nSlices;

    //Create Projection if several slices
    if(localSliceNumber>1){
        run("Z Project...", "projection=[Max Intensity]");
    }

    saveAs("Jpeg",
            FolderOutput + NameFile + "_report.jpg");

    run("Close");


    /*
    ============================================================================
                    CREATE THE DISTRIBUTION FILES
    ============================================================================
    */

    //Force variables to have at least one values
    if (NValuesCorr == ""){
        NValuesCorr += "" + 0 + "-";
    }

    if (NValues == ""){
        NValues += "" + 0 + "-";
    }

    if (INValues == ""){
        INValues += "" + 0 + "-";
    }

    if (EValuesCorr == ""){
        EValuesCorr += "" + 0 + "-";
    }

    if (EValues == ""){
        EValues += "" + 0 + "-";
    }

    if (IEValues == ""){
        IEValues += "" + 0 + "-";
    }

    //Select the window used to detect
    selectWindow("Raw");

    //Select and Deselect the last ROI to enable to save the set
    roiManager("Select", roiManager("count")-1);
    roiManager("Deselect");
    roiManager("Save", FolderOutput + NameFile + "_RoiSet.zip");

    //Get the time finger print of the end of the analysis
    getDateAndTime(year,
                    month,
                    dayOfWeek,
                    dayOfMonth,
                    hour,
                    minute,
                    second,
                    msec);
    mystop = "" + year + "/" + (month+1) + "/" + dayOfMonth + " ";
    mystop += "" + hour + ":" + minute;

    //Update the variables for the MarkDown Report
    mybrains += "Manual Selection: **" + (ANP/1000000) + "**\n\n";
    TotalNeuropilSurface = myslices * ANP/1000000;

    //initialize the total surface of particles for the tissue and Manual ROI
    BrainLDSurface = 0;
    NPLDSurface = 0;

    //Update values for all slices
    for(np = 0; np<LDperSlice.length; np++){
        BrainLDSurface += AreaLDperSlice[np];
        NPLDSurface += AreaLDNPperSlice[np];
        AreaLDperSlice[np] = 100 * AreaLDperSlice[np]/(ABrains[np+1]*1000000);
        AreaLDNPperSlice[np] = 100 * AreaLDNPperSlice[np]/ANP;

        //Update the variables for the MarkDown Report
        mybrains += "- Slice " + (np+1) + ": **" + LDperSlice[np] + "** Particles in total.";
        mybrains += "(" + AreaLDperSlice[np] + "% coverage of Tissue)\n";
        mybrains += " **" + LDNPperSlice[np] + "** in the Manual Selection ";
        mybrains += "(" + AreaLDNPperSlice[np] + "% coverage of Manual Selection)\n";
    }
    mybrains += "\n";

    //Generate Graphs and Distributions
    MakeDistribution();

    //Update Report
    MD = UpdateMD(MD);

    //Save MD and CSV
    File.saveString(MD, myRoot + FP + NameFile + "_REPORT.md");
    File.saveString(myCSV, FolderOutput + NameFile + "_data.csv");

    //Close all non required images.
    PathClear = getDirectory("macros");
    PathClear += "Droplets"+File.separator;
    PathClear += "Close_Images.java";
    runMacro(PathClear);

    //Erase ROI
    roiManager("reset");

/*
===============================================================================
                            FUNCTIONS
===============================================================================
*/

function MakeDistribution(){

    //Make all Distribution, with specific values, colors,...

    //Min X value
    Arg1 = newArray(0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                    );

    //Max X value
    Arg2 = newArray(SizeMaxC * pixelWidth * pixelHeight,
                    SizeMaxC * pixelWidth * pixelHeight * 20,
                    20000,
                    SizeMaxC * pixelWidth * pixelHeight,
                    SizeMaxC * pixelWidth * pixelHeight * 20,
                    20000,
                    SizeMaxC * pixelWidth * pixelHeight,
                    SizeMaxC * pixelWidth * pixelHeight * 20,
                    20000
                    );

    //Label of the X axis
    Arg3 = newArray("Droplet (microns)",
                    "Droplet size per million microns Brain",
                    "Mean grey values",
                    "Droplet (microns)",
                    "Droplet size per million microns Brain",
                    "Mean grey values",
                    "Droplet (microns)",
                    "Droplet size per million microns Brain",
                    "Mean grey values"
                    );

    //Suffix for the files to be generated
    Arg4 = newArray("_Values_ALL",
                    "_Corrected_Values_ALL",
                    "_Intensities_ALL",
                    "_Values_NP",
                    "_Corrected_Values_NP",
                    "_Intensities_NP",
                    "_Values_Non-NP",
                    "_Corrected_Values_Non-NP",
                    "_Intensities_Non-NP"
                    );

    //Values
    Arg5 = newArray(AValues,
                    AValuesCorr,
                    IValues,
                    NValues,
                    NValuesCorr,
                    INValues,
                    EValues,
                    EValuesCorr,
                    IEValues);

    //Colors
    Arg6 = newArray("magenta",
                    "magenta",
                    "magenta",
                    "cyan",
                    "cyan",
                    "cyan",
                    "orange",
                    "orange",
                    "orange"
                    );

    Arg7 = newArray(TotalBrainSurface,
                    TotalNeuropilSurface,
                    TotalBrainSurface -TotalNeuropilSurface,
                    TotalBrainSurface,
                    TotalNeuropilSurface,
                    TotalBrainSurface -TotalNeuropilSurface,
                    TotalBrainSurface,
                    TotalNeuropilSurface,
                    TotalBrainSurface -TotalNeuropilSurface);

    //Generate all Distributions
    for(index=0; index<lengthOf(Arg1); index++){

        ARG2 = "" + Arg1[index] + "*";
        ARG2 += "" + Arg2[index] + "*";
        ARG2 += "" + nBins + "*";
        ARG2 += "" + Arg3[index] + "*";
        ARG2 += "" + FolderOutput + NameFile + "*";
        ARG2 += "" + Arg4[index] + "*";
        ARG2 += Arg5[index] + "*";
        ARG2 += Arg6[index] + "*";
        ARG2 += "" + Arg7[index];

        runMacro(PathDD, ARG2);

    }

}

/*
================================================================================
*/

function UpdateMD(MD){

    //Replace all Keywords in the MarkDown report file
    MeanBrainLDSurface = BrainLDSurface / myslices;

    //Arrays of Keywords and Values
    myKeywords = newArray("MYIMAGE",
                        "MYANALYSISMODE",
                        "MYDATE",
                        "MYSTOP",
                        "MYOS",
                        "MYJAVA",
                        "MYIJ",
                        "MYRESOLUTION",
                        "MYSLICES",
                        "MYSTART",
                        "MYEND",
                        "MYSELECTION",
                        "MYREFERENCE",
                        "XYTHRESHOLD",
                        "ZTHRESHOLD",
                        "MYITERATIONS",
                        "MYFACTOR",
                        "MINSURF",
                        "MAXSURF",
                        "SURFMAXC",
                        "MINCIRC",
                        "MAXCIRC",
                        "MYDROPLETS",
                        "MYNEUROPIL",
                        "MYBRAINS",
                        "TOTALBRAINSURFACE",
                        "MEANBRAINSURFACE",
                        "MEANBRAINLDSURFACE",
                        "MEANLDSURFACEBRAIN",
                        "BRAINLDPERSURFACE",
                        "TOTALNP",
                        "NPLDTOTALSURFACE",
                        "NPLDMEANSURFACE",
                        "NPLDPERSURFACE",
                        "MYGIF",
                        "DISTRAWJPG",
                        "DISTRAWcumJPG",
                        "DISTJPG",
                        "DISTcumJPG",
                        "DISTIJPG",
                        "DISTIcumJPG",
                        "DISTNPRAWJPG",
                        "DISTNPRAWcumJPG",
                        "DISTNPJPG",
                        "DISTNPcumJPG",
                        "DISTNPIcumJPG",
                        "DISTNNPRAWJPG",
                        "DISTNNPRAWcumJPG",
                        "DISTNNPJPG",
                        "DISTNPIJPG",
                        "DISTNNPcumJPG",
                        "DISTNNPIJPG",
                        "DISTNNPIcumJPG");

    myValues = newArray("" + myimage,
                        myAnalysis,
                        mydate,
                        mystop,
                        getInfo("os.name"),
                        getInfo("java.version"),
                        IJVersion,
                        reso,
                        "" + myslices,
                        "" + Sstart,
                        "" + Send,
                        Selection,
                        "" + resoRef,
                        "" + (seuil* ImResolution) + " microns",
                        "" + (zDistance) + " slices",
                        "" + Iterations,
                        "" + enlargement + " pixels",
                        "" + (SizeMin * ImResolution) + " microns",
                        "" + (SizeMax * ImResolution) + " microns",
                        "" + (SizeMaxC * ImResolution) + " microns",
                        "" + CircMinC,
                        "" + CircMaxC,
                        "" + totalLD,
                        "" + numberNP,
                        "" + mybrains,
                        "" + TotalBrainSurface,
                        "" + TotalBrainSurface/myslices,
                        "" + MeanBrainLDSurface/1000000,
                        "" + BrainLDSurface / totalLD,
                        "" + totalLD / TotalBrainSurface,
                        "" + TotalNeuropilSurface,
                        "" + (NPLDSurface/1000000) / myslices,
                        "" + NPLDSurface / numberNP,
                        "" + numberNP / TotalNeuropilSurface,
                        FolderOutputRelative + NameFile + "_report.jpg",
                        FolderOutputRelative + NameFile + "_Values_ALL_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Values_ALL_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_ALL_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_ALL_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_ALL_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_ALL_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Values_NP_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Values_NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_NP_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Values_Non-NP_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Values_Non-NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_Non-NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_NP_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Corrected_Values_Non-NP_Cumul_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_Non-NP_Distribution.jpg",
                        FolderOutputRelative + NameFile + "_Intensities_Non-NP_Cumul_Distribution.jpg");

    for (index=0; index<myKeywords.length; index++){
        MD = replace(MD, myKeywords[index], myValues[index]);
    }
    return MD;
}

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
