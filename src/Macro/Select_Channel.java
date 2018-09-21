macro "Select_Channel.java"{

    //Permit to identify which channel to analyze

    //Retrieve arguments into an array
    Argument = getArgument();
    Arguments = split(Argument, "*");

    //Path of the file containing the file to analyze
    myAnalysis = Arguments[0];

    //Are old taylored parameters reused
    myReuse = Arguments[1];

    //Open the file containing the path of the stacks as a String
    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");

    //The whole process is performed ONLY if no parameters
    //are re-used
    if (myReuse == "NO"){
        setBatchMode(true);

        //Select first image file
        Path = FileList[0];

        //Command for Bioformat Importer
        CMD1 = "open=[";
        CMD1 += Path + "]";
        CMD1 += " autoscale";
        CMD1 += " color_mode=Default";
        CMD1 += " rois_import=[ROI manager]";
        CMD1 += " view=Hyperstack stack_order=XYCZT";
        run("Bio-Formats Importer", CMD1);

        //Get the name of the image
        Titre = getTitle;

        //Detecting Stacks
        if (Stack.isHyperstack==1){

            //Get stack dimensions
            Stack.getDimensions(width,
                                height,
                                channels,
                                slices,
                                frames);

            //Create array of channel name
            channelsNames = newArray();

            //Split channels
            run("Split Channels");

            //Feed the array and enhance the display of each channel
            for (c=1; c<=channels; c++){
                channelsNames = Array.concat(channelsNames,
                                            "C" + c + "-");
                selectWindow("C" + c + "-" + Titre);
                run("Enhance Contrast", "saturated=0.35");
                setBatchMode("show");
                selectWindow("C" + c + "-" + Titre);
                //Display the current channel at a specific location and size
                setLocation(400 * (c-1),
                            0,
                            400,
                            height*(400/width));
            }

            //Attribute the channel for analysis
            Dialog.create("Choose the channel to use.");
            Dialog.addChoice("Channels: ",
                            channelsNames,
                            channelsNames[0]);
            Dialog.show();
            myChannel = Dialog.getChoice();

        }else{
            myChannel = "C0-";
        }

        //Close all non required images.
        PathM3 = getDirectory("macros");
        PathM3 += "Droplets"+File.separator;
        PathM3 += "Close_Images.java";
        runMacro(PathM3);
    }

    //Retur the prefix of the channel to treat
    return myChannel;

}
