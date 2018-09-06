Automated Detection of Lipid Droplets in *Drosophila M.* brain
===


|![Example original](doc/Original.jpg)|![Example treated](doc/Treated.jpg)|![Example distribution](doc/Distribution.jpg)|![Example corrected distribution](doc/CDistribution.jpg)
|-------------------------------------|-----------------------------------|-----------------------------------|-----------------------------------|
|**ORIGINAL**   |**TREATED**   |**DISTRIBUTION**   |**STATISTICS ENABLED**   |



**Contributors**
--

| ![LBMC Logo](doc/Logo_LBMC.jpg) ![CNRS Logo](doc/Logo_cnrs.jpg) ![ENS Logo](doc/Logo_ens.jpg) ||
|-----------------------------|------------|
|**CLUET David**|     [david.cluet@ens-lyon.fr](david.cluet@ens-lyon.fr)|


**Requirements**
--
The `LIPID_DROPLETS` macro requires `ImageJ v1.49g` or higher ([Download](https://imagej.nih.gov/ij/download.html)).

For ImageJ, the conversion of the analyzed stacks into animated GIFs requires the ([Gif-Stack-Writer Plugin](https://imagej.nih.gov/ij/plugins/gif-stack-writer.html)).

To read Markdown files, use the `Markdown Preview Plus` extension for `Chrome`. In the `Extension menu` allow access to files URL.

**User guide**
===


**1) INSTALLATION**
-
The `LIPID_DROPLETS` macro requires can be automatically installed with all required files in `ImageJ` and `FIJI`. Please follow the specific instructions described below.


|[ImageJ Logo](doc/IJ.jpg)
-
1. Open `ImageJ`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation.ijm` file on `ImageJ` Menu bar to open it.
4. In the Menu bar of the macro select the `Macros/Run Macro` option.
5. The window will be closed automatically and all required files will be installed in the `ImageJ/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `ImageJ` to refresh the Menu bar.


|[FIJI Logo](doc/FIJI.jpg)
-
1. Open `FIJI`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation_Fiji.ijm` file on `FIJI` Menu bar to open it.
4. In the console select the `Run` option.
5. All required files will be installed in the `Fiji.app/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `FIJI` to refresh the Menu bar.


**2) UPDATE**
-
Follow the same instructions as for the installation process.

**3) LAUNCH AN ANALYSIS**
-

Click on the `Plugins/Macros/LIPID_DROPLETS` shortcut.

![Shortcut](doc/Shortcut.jpg)

The macro is initiated. The welcome widow is prompted.

![Wecome](doc/Wecome.jpg)

The next window will propose different pre-set analysis modes:

![Modes](doc/Modes.jpg)

The settings are saved in the `settings.csv` file located in your `ImageJ/macros/Droplets` folder.
They contain the key parameters for the analysis and are organized as following:

|Name|Extension of the files to analyze|Reference resolution (micron/pixel) in X|Reference resolution (micron/pixel) in Y|Distance xy in pixels between 2 particles|Distance in z between 2 particles|Minimum size in pixel|Maximum size in pixel|Maximum size (to exclude big fat bodies)|Minimum circularity|Maximum circularity|Number of Iterations|Zone for enlargement (in pixel) and erasing|Number of bins for distributions|Zone of analysis|Minimal number of new Particles|
|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|--|
|Lipid Droplets Brain|.czi|0.156|0.156|5|5|7|15000|500|0.5|1|3|5|50|?|50|
|Lipid Droplets Retina|.czi|0.156|0.156|5|5|6.98|15000|821|82|0.5|1|2|5|50|Manual ROI|50|
|Repo|.czi|0.156|0.156|5|5|7|15000|500|0.5|1|3|5|50|Whole tissue|50|

If you respect this structure you can add your own settings.The various parameters will be described in the `Graphic User Interface` section.
