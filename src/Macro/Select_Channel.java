macro "Select_Channel.java"{

    Argument = getArgument();
    Arguments = split(Argument, "*");
    myAnalysis = Arguments[0];
    myReuse = Arguments[1];

    RawList = File.openAsString(myAnalysis);
    FileList = split(RawList, "\n");

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

            for (c=1; c<=channels; c++){
                channelsNames = Array.concat(channelsNames,
                                            "C" + c + "-");
                selectWindow("C" + c + "-" + Titre);
                run("Enhance Contrast", "saturated=0.35");
                setBatchMode("show");
                selectWindow("C" + c + "-" + Titre);
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
            Dialog.show()
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

    return myChannel;

}
