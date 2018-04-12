macro "Lipid_Droplets"{

/*
===============================================================================
                            MAIN VARIABLES
===============================================================================
*/

IJVersion = getVersion();

//Arguments

Argument = getArgument();
Arguments = split(Argument, "*");

Selection = Arguments[0];
ResWref = parseFloat(Arguments[1]);
ResHref = parseFloat(Arguments[2]);
seuil = parseFloat(Arguments[3]);
zDistance = parseFloat(Arguments[4]);
SizeMin = parseFloat(Arguments[5]);
SizeMax = parseFloat(Arguments[6]);
SizeMaxC = parseFloat(Arguments[7]);
CircMinC = parseFloat(Arguments[8]);
CircMaxC = parseFloat(Arguments[9]);
Iterations = parseFloat(Arguments[10]);
enlargement = parseFloat(Arguments[11]);
nBins = parseFloat(Arguments[12]);

myAnalysis = Arguments[13];

Sstart = parseFloat(Arguments[14]);
Send = parseFloat(Arguments[15]);
NeuroPilXtext = Arguments[16];
NeuroPilX = split(NeuroPilXtext, "-");
NeuroPilYtext = Arguments[17];
NeuroPilY = split(NeuroPilYtext, "-");

Path = Arguments[18];
myRoot = Arguments[19];
myProgress = parseFloat(Arguments[20]);
FPT = Arguments[21];
FP = Arguments[22];

/*
===============================================================================
                            CORE PROGRAM
===============================================================================
*/

    //Close all non required images.
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);

    //Clean roiManager
    roiManager("reset");

    //Create the Outputfolder
    Parent = File.getParent(Path) + File.separator();
    NameFile = File.getName(Path);
    NameFile = substring(NameFile,
                            0,
                            lastIndexOf(NameFile, ".")
                            );
    FolderOutput = Parent + FP + "_" + NameFile + File.separator();

    FolderOutputRelative = File.separator() + FP + "_" + NameFile + File.separator();


    if (File.exists(FolderOutput)!=1){
        File.makeDirectory(FolderOutput);
    }

    //Open the Markdown File
    PathMD = getDirectory("macros");
    PathMD += "Droplets"+File.separator;
    PathMD += "LayOut.md";
    MD = File.openAsString(PathMD);
    myCSV = "Name" + "\t" + "Slice" + "\t" + "X" + "\t" + "Y" + "\t" + "Area um2" + "\t" + "Corrected um2" + "\t" + "Mean Intensity" + "\n";

    //Command for Bioformat Importer
    CMD1 = "open=[";
    CMD1 += Path + "]";
    CMD1 += " autoscale";
    CMD1 += " color_mode=Default";
    CMD1 += " rois_import=[ROI manager]";
    CMD1 += " view=Hyperstack stack_order=XYCZT";
    run("Bio-Formats Importer", CMD1);

    myimage = getTitle();
    getPixelSize(unit, pixelWidth, pixelHeight);
    reso = "" + pixelWidth + " " + unit + " x " + pixelHeight + " " + unit;
    resoRef = "" + ResWref + " " + unit + " x " + ResHref + " " + unit;

    //Detecting Stacks
    if (Stack.isHyperstack==1){

        //Split channels
        run("Split Channels");

        //Keep only the first channel to perform analysis
        Bodipy = "C1-" + myimage;
        Tissue = "C2-" + myimage;
        selectWindow(Bodipy);
        rename(myimage);
        if (myAnalysis != "Repo"){
            selectWindow(Tissue);
            close();
        }else{
            selectWindow(Tissue);
            rename("Brain");
        }

    }

    //Recalibrating the area values depending on resoltion.
    RefResolution = ResWref*ResHref;
    ImResolution = pixelWidth*pixelHeight;
    Ratio = RefResolution/ImResolution;
    SizeMin = SizeMin * Ratio;
    SizeMax = SizeMax * Ratio;
    SizeMaxC = SizeMaxC * Ratio;

    //UPDATE THE REPORT FILE
    MD = replace(MD, "MYSTART", "" + Sstart);
    MD = replace(MD, "MYEND", "" + Send);
    MD = replace(MD, "MYSELECTION", Selection);
    MD = replace(MD, "MYREFERENCE", "" + resoRef);
    MD = replace(MD, "XYTHRESHOLD", "" + (seuil* ImResolution) + " microns");
    MD = replace(MD, "ZTHRESHOLD", "" + (zDistance) + " slices");
    MD = replace(MD, "MYITERATIONS", "" + Iterations);
    MD = replace(MD, "MYFACTOR", "" + enlargement + " pixels");
    MD = replace(MD, "MINSURF", "" + (SizeMin * ImResolution) + " microns");
    MD = replace(MD, "MAXSURF", "" + (SizeMax * ImResolution) + " microns");
    MD = replace(MD, "SURFMAXC", "" + (SizeMaxC * ImResolution) + " microns");
    MD = replace(MD, "MINCIRC", "" + CircMinC);
    MD = replace(MD, "MAXCIRC", "" + CircMaxC);
    MD = replace(MD, "MYIMAGE", "" + myimage);

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
    run("Duplicate...", "title=Intensity duplicate");

    //Crop the Stack
    PathM1 = getDirectory("macros");
    PathM1 += "Droplets"+File.separator;
    PathM1 += "Stack_Editing.java";

    ARG1 = "Raw" + "\t";
    ARG1 += "" + Sstart + "\t";
    ARG1 += "" + Send + "\t";

    runMacro(PathM1, ARG1);

    if (myAnalysis == "Repo"){
        selectWindow("Brain");

        ARG1 = "Brain" + "\t";
        ARG1 += "" + Sstart + "\t";
        ARG1 += "" + Send + "\t";

        runMacro(PathM1, ARG1);
    }




    //Duplicate the stack for Brain detection
    selectWindow("Raw");
    myslices = nSlices;
    MD = replace(MD, "MYSLICES", "" + myslices);
    if (myAnalysis != "Repo"){
        run("Duplicate...", "title=Brain duplicate");
    }

    roiManager("reset");

    Detect_Brain("Brain", FolderOutput + NameFile);


    //Create array of Brain areas. Start with 0 to have same index as slices
    ABrains = newArray();
    ABrains = Array.concat(ABrains, 0);

    LDperSlice = newArray(nSlices);
    LDNPperSlice = newArray(nSlices);
    AreaLDperSlice = newArray(nSlices);
    AreaLDNPperSlice = newArray(nSlices);

    mybrains = "";

    TotalBrainSurface = 0;

    for (S=1; S<=nSlices; S++){
        setSlice(S);
        ROIopen(FolderOutput + NameFile + "_Brain_Slices.txt", S-1);
        getStatistics(area);
        ABrains = Array.concat(ABrains, area * pixelWidth * pixelHeight/1000000);
        TotalBrainSurface += area * pixelWidth * pixelHeight/1000000;
        mybrains += "Brain slice " + S + " : **" + ABrains[S] + "**\n\n";
    }


    Array.getStatistics(ABrains, min, max, mean, stdDev);
    minBrain = min;

    //Array.show(ABrains);

    selectWindow("Brain");
    close();
    roiManager("reset");

    //Draw Neuropil
    selectWindow("Raw");
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");
    roiManager("Select", 0);
    List.setMeasurements;
    ANP = List.getValue("Area") * pixelWidth * pixelHeight;
    XNP = List.getValue("X");
    YNP = List.getValue("Y");


    ACorr = ANP/ABrains[nSlices];
    myCSV += "" + "Neuropil" + "\t" + nSlices + "\t";
    myCSV +=  "" + XNP + "\t" + YNP + "\t" + ANP + "\t" + ACorr + "\n";

    newImage("Neuropil", "8-bit white", W, H, 1);
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    setForegroundColor (0,0,0);
    run("Fill");

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

        for (S=1; S<=nSlices; S++){
            setSlice(S);
            makeRectangle(0,0,W,H);
            setAutoThreshold("MaxEntropy dark");
            run("Analyze Particles...", "size="+SizeMin+"-"+SizeMax+" add slice");
        }



        if (nROI<roiManager("count")){

            //Remove all non brain particles
            if (Selection == "Brain"){
                for (p=0; p<roiManager("count"); p++){
                    setForegroundColor(0, 0, 0);
                    selectWindow("Brain-Shape");
                    roiManager("Select", p);
                    List.setMeasurements;
                    M = List.getValue("Mean");
                    if (M!=0){
                        selectWindow("Raw");
                        roiManager("Select", p);
                        run("Enlarge...", "enlarge=" + enlargement);
                        run("Fill", "slice");
                        roiManager("Delete");
                        p = p -1;
                    }
                }
            }

            if (Selection == "Manual ROI"){
                for (p=0; p<roiManager("count"); p++){
                    setForegroundColor(0, 0, 0);
                    selectWindow("Neuropil");
                    roiManager("Select", p);
                    List.setMeasurements;
                    M = List.getValue("Mean");
                    if (M!=0){
                        selectWindow("Raw");
                        roiManager("Select", p);
                        run("Enlarge...", "enlarge=" + enlargement);
                        run("Fill", "slice");
                        roiManager("Delete");
                        p = p -1;
                    }
                }
            }

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

    //Reinitialisation of arrays containinf number of LD and total surface
    for(np = 0; np<LDperSlice.length; np++){
        LDperSlice[np] = 0;
        AreaLDperSlice[np] = 0;
        LDNPperSlice[np] = 0;
        AreaLDNPperSlice[np] = 0;
    }

    numberNP = 0;
    for(i=0; i<roiManager("count"); i++){
        selectWindow("Raw");
        roiManager("Select",i);
        currentSlice = getSliceNumber - 1;
        List.setMeasurements;
        ALD = List.getValue("Area") * pixelWidth * pixelHeight;
        LDperSlice[currentSlice] = LDperSlice[currentSlice] + 1;
        AreaLDperSlice[currentSlice] = AreaLDperSlice[currentSlice] + ALD;
        selectWindow("Neuropil");
        roiManager("Select",i);
        myName = Roi.getName;
        getStatistics(area, mean);
        if (mean<255){
            roiManager("Select",i);
            roiManager("Rename", "NP_"+myName);
            numberNP += 1;
            LDNPperSlice[currentSlice] = LDNPperSlice[currentSlice] + 1;
            AreaLDNPperSlice[currentSlice] = AreaLDNPperSlice[currentSlice] + ALD;
            }
    }

    totalLD = roiManager("count");
    MD = replace(MD, "MYDROPLETS", "" + totalLD);
    MD = replace(MD, "MYNEUROPIL", "" + numberNP);

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
        ROIopen(FolderOutput + NameFile + "_Brain_Slices.txt", S-1);
        run("Draw", "slice");
    }

    setForegroundColor(255,0,255);
    for(i=0; i<roiManager("count"); i++){
        selectWindow("Intensity");
        roiManager("Select",i);
        roiName=Roi.getName;
        List.setMeasurements;
        A = List.getValue("Area") * pixelWidth * pixelHeight;
        X = List.getValue("X");
        Y = List.getValue("Y");
        Intensity = List.getValue("Mean");
        ACorr = A/ABrains[getSliceNumber];
        myCSV += "" + roiName + "\t" + getSliceNumber + "\t";
        myCSV += "" + X + "\t" + Y + "\t" + A + "\t" + ACorr + "\t";
        myCSV += "" + Intensity + "\n";
        AValues += "" + A + "-";
        AValuesCorr += "" + ACorr + "-";
        IValues += "" + Intensity + "-";
        if (lastIndexOf(roiName,"NP_") != -1){
            //Neuropil only
            NValues += "" + A + "-";
            NValuesCorr += "" + ACorr + "-";
            INValues += "" + Intensity + "-";
        }else{
            //Non Neuropil only
            EValues += "" + A + "-";
            EValuesCorr += "" + ACorr + "-";
            IEValues += "" + Intensity + "-";
        }
        selectWindow("Report");
        roiManager("Select",i);
        run("Draw", "slice");
    }

    setForegroundColor(0,255,255);
    selectWindow("Report");
    makeSelection("polygon", NeuroPilX, NeuroPilY);
    roiManager("Add");
    roiManager("Select", roiManager("count")-1);
    roiManager("Rename", "Neuropil");
    run("Draw", "stack");

    //saveAs("Tiff", Path + "_report.tif");

    if (lastIndexOf(IJVersion,"/") == -1){
        //ImageJ
        CMD = "save=[" + FolderOutput + NameFile + "_report.gif]";
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
        CMD += "" + "filename=[" + FolderOutput + NameFile + "_report.gif]";
        run("Animated Gif ... ",
            CMD);
    }

    run("Close");
    MD = replace(MD, "MYGIF", FolderOutputRelative + NameFile + "_report.gif");


    //Draw Distribution
    PathM2 = getDirectory("macros");
    PathM2 += "Droplets"+File.separator;
    PathM2 += "Distribution.java";


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

    //Create the result filesCircMinC, SizeMaxC
    selectWindow("Raw");
    roiManager("Select", roiManager("count")-1);
    roiManager("Deselect");
    roiManager("Save", FolderOutput + NameFile + "_RoiSet.zip");

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
    MD = replace(MD, "MYSTOP", mystop);


    mybrains += "Neuropil : **" + (ANP/1000000) + "**\n\n";
    TotalNeuropilSurface = myslices * ANP/1000000;

    BrainLDSurface = 0;
    NPLDSurface = 0;

    for(np = 0; np<LDperSlice.length; np++){
        BrainLDSurface += AreaLDperSlice[np];
        NPLDSurface += AreaLDNPperSlice[np];
        AreaLDperSlice[np] = 100 * AreaLDperSlice[np]/(ABrains[np+1]*1000000);
        AreaLDNPperSlice[np] = 100 * AreaLDNPperSlice[np]/ANP;
        mybrains += "- Slice " + (np+1) + ": **" + LDperSlice[np] + "** LDs in total.";
        mybrains += "(" + AreaLDperSlice[np] + "% coverage of Brain)\n";
        mybrains += " **" + LDNPperSlice[np] + "** in the Neuropil ";
        mybrains += "(" + AreaLDNPperSlice[np] + "% coverage of Neuropil)\n";

    }
    mybrains += "\n";
    MD = replace(MD, "MYBRAINS", "" + mybrains);

    mycolor = "magenta";
    //Raw Distribution
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet (microns)" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Values_ALL" + "*";
    ARG2 += AValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalBrainSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTRAWJPG", FolderOutputRelative + NameFile + "_Values_ALL_Distribution.jpg");
    MD = replace(MD, "DISTRAWcumJPG", FolderOutputRelative + NameFile + "_Values_ALL_Cumul_Distribution.jpg");

    //Corrected Distribution (per million of pixel of brain surface)
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight * 20) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet size per million microns Brain" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Corrected_Values_ALL" + "*";
    ARG2 += AValuesCorr + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalBrainSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTJPG", FolderOutputRelative + NameFile + "_Corrected_Values_ALL_Distribution.jpg");
    MD = replace(MD, "DISTcumJPG", FolderOutputRelative + NameFile + "_Corrected_Values_ALL_Cumul_Distribution.jpg");

    //Intensities
    ARG2 = "" + 0 + "*";
    ARG2 += "" + 20000 + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Mean grey values" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Intensities_ALL" + "*";
    ARG2 += IValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalBrainSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTIJPG", FolderOutputRelative + NameFile + "_Intensities_ALL_Distribution.jpg");
    MD = replace(MD, "DISTIcumJPG", FolderOutputRelative + NameFile + "_Intensities_ALL_Cumul_Distribution.jpg");

    mycolor = "cyan";
    //Raw Distribution Neuropil
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet (microns)" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Values_NP" + "*";
    ARG2 += NValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalNeuropilSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNPRAWJPG", FolderOutputRelative + NameFile + "_Values_NP_Distribution.jpg");
    MD = replace(MD, "DISTNPRAWcumJPG", FolderOutputRelative + NameFile + "_Values_NP_Cumul_Distribution.jpg");

    //Corrected Distribution for Neuropil
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight * 20) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet size per million microns Brain" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Corrected_Values_NP" + "*";
    ARG2 += NValuesCorr + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalNeuropilSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNPJPG", FolderOutputRelative + NameFile + "_Corrected_Values_NP_Distribution.jpg");
    MD = replace(MD, "DISTNPcumJPG", FolderOutputRelative + NameFile + "_Corrected_Values_NP_Cumul_Distribution.jpg");

    //Intensities
    ARG2 = "" + 0 + "*";
    ARG2 += "" + 20000 + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Mean grey values" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Intensities_NP" + "*";
    ARG2 += INValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + TotalNeuropilSurface;

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNPIJPG", FolderOutputRelative + NameFile + "_Intensities_NP_Distribution.jpg");
    MD = replace(MD, "DISTNPIcumJPG", FolderOutputRelative + NameFile + "_Intensities_NP_Cumul_Distribution.jpg");

    mycolor = "orange";
    //Raw Distribution Non-Neuropil
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet (microns)" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Values_Non-NP" + "*";
    ARG2 += EValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + (TotalBrainSurface -TotalNeuropilSurface);

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNNPRAWJPG", FolderOutputRelative + NameFile + "_Values_Non-NP_Distribution.jpg");
    MD = replace(MD, "DISTNNPRAWcumJPG", FolderOutputRelative + NameFile + "_Values_Non-NP_Cumul_Distribution.jpg");

    //Corrected Distribution for non-Neuropil
    ARG2 = "" + 0 + "*";
    ARG2 += "" + (SizeMaxC * pixelWidth * pixelHeight * 20) + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Droplet size per million microns Brain" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Corrected_Values_Non-NP" + "*";
    ARG2 += EValuesCorr + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + (TotalBrainSurface -TotalNeuropilSurface);

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNNPJPG", FolderOutputRelative + NameFile + "_Corrected_Values_Non-NP_Distribution.jpg");
    MD = replace(MD, "DISTNNPcumJPG", FolderOutputRelative + NameFile + "_Corrected_Values_Non-NP_Cumul_Distribution.jpg");

    //Intensities
    ARG2 = "" + 0 + "*";
    ARG2 += "" + 20000 + "*";
    ARG2 += "" + nBins + "*";
    ARG2 += "" + "Mean grey values" + "*";
    ARG2 += "" + FolderOutput + NameFile + "*";
    ARG2 += "" + "_Intensities_Non-NP" + "*";
    ARG2 += IEValues + "*";
    ARG2 += mycolor + "*";
    ARG2 += "" + (TotalBrainSurface -TotalNeuropilSurface);

    runMacro(PathM2, ARG2);
    MD = replace(MD, "DISTNNPIJPG", FolderOutputRelative + NameFile + "_Intensities_Non-NP_Distribution.jpg");
    MD = replace(MD, "DISTNNPIcumJPG", FolderOutputRelative + NameFile + "_Intensities_Non-NP_Cumul_Distribution.jpg");










    //Brain Area : n slices area sum
    MD = replace(MD, "TOTALBRAINSURFACE", "" + TotalBrainSurface);

    //Mean Brain
    MD = replace(MD, "MEANBRAINSURFACE", "" + TotalBrainSurface/myslices);

    //Brain LD Surface : Mean of the % coverage of Brain * Brain Area / 100
    MeanBrainLDSurface = BrainLDSurface / myslices;
    MD = replace(MD, "MEANBRAINLDSURFACE", "" + MeanBrainLDSurface/1000000);

    //Brain LD Mean Surface : Brain LD surface / Brain LD number
    MD = replace(MD, "MEANLDSURFACEBRAIN", "" + BrainLDSurface / totalLD);

    //Brain LD number / surface : Brain LD number / Brain Area
    MD = replace(MD, "BRAINLDPERSURFACE", "" + totalLD / TotalBrainSurface);

    //Neuropil Area : 3*Neuropil
    MD = replace(MD, "TOTALNP", "" + TotalNeuropilSurface);

    //Neuropil LD surface : Mean of the % coverage of neuropil * Neuropil Area / 100
    MD = replace(MD, "NPLDTOTALSURFACE", "" + (NPLDSurface/1000000) / myslices);

    //Neuropil LD Mean Surface : Neuropil LD surface / Neuropi LD number
    MD = replace(MD, "NPLDMEANSURFACE", "" + NPLDSurface / numberNP );

    //Neuropil LD number / surface : Neuropil LD number /  Neuropil Area
    MD = replace(MD, "NPLDPERSURFACE", "" + numberNP / TotalNeuropilSurface);

    if (myAnalysis == "Repo"){
        MD = replace(MD, "LD", "REPO");
        MD = replace(MD, "droplets", "REPO");
    }

    //Save MD and CSV
    File.saveString(MD, myRoot + FP + NameFile + "_REPORT.md");
    File.saveString(myCSV, FolderOutput + NameFile + "_data.csv");

    //Close all non required images.
    PathM3 = getDirectory("macros");
    PathM3 += "Droplets"+File.separator;
    PathM3 += "Close_Images.java";
    runMacro(PathM3);

    //Erase ROI
    roiManager("reset");

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

    setForegroundColor(0, 0, 0);

    selectWindow(myImage);
    W = getWidth();
    H = getHeight();
    N = nSlices;

    newImage("Brain-Shape", "8-bit white", W, H, N);



    for (S=1; S<=nSlices; S++){

        selectWindow(myImage);
        nROI = roiManager("count");
        setSlice(S);
        makeRectangle(0,0,getWidth,getHeight);
        run("Gaussian Blur...", "sigma=20 slice");
        //run("Gaussian Blur...", "sigma=5 slice");
        setAutoThreshold("Huang dark");
        run("Analyze Particles...", "size=100000-Infinity pixel show=Nothing add slice");

        if (roiManager("count") > nROI+1){

            Highlander(nROI);
            roiManager("Select", nROI);
            roiManager("Rename", "Brain slice "+S);

            selectWindow("Brain-Shape");
            setSlice(S);
            roiManager("Select", nROI);
            run("Fill", "slice");

        }else if(roiManager("count") == nROI+1){

            roiManager("Select", nROI);
            roiManager("Rename", "Brain slice "+S);

            selectWindow("Brain-Shape");
            setSlice(S);
            roiManager("Select", nROI);
            run("Fill", "slice");

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

        if(Ar>SizeMaxC){
            //Clean false positive
            Erase = 1;
        }else{
            for(N=n+1; N<roiManager("count"); N++){
                message = "Iteration " + it;
                message += " ROI " + n;
                message += " out of " + roiManager("count");
                message += " Batch started " + FPT;
                showStatus(message);
                showProgress(myProgress);
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
                if ((abs(S-Sr)<=zDistance) && (abs(S-Sr)>=0)){
                    d = sqrt( (X-Xr)*(X-Xr) + (Y-Yr)*(Y-Yr) );
                    if (d<seuil){
                        if(A>=Ar){
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
