**Automated Detection of labeled particles in microscopic stacks of *Drosophila M.* tissues**
===

**Abstract**
--

Under development

|![Example original](doc/Original.jpg)|![Example treated](doc/Treated.jpg)|![Example distribution](doc/Distribution.jpg)|![Example corrected distribution](doc/CDistribution.jpg)
|-------------------------------------|-----------------------------------|-----------------------------------|-----------------------------------|
|**ORIGINAL**   |**TREATED**   |**DISTRIBUTION**   |**STATISTICS ENABLED**   |


**License**
--

Copyright CNRS 2013


>This software is a computer program whose purpose is to **automatically detect labeled Particles in microscopic stacks of Drosophila M. tissues**.
>
>This software is governed by the CeCILL  license under French law and abiding
by the rules of distribution of free software. You can use, modify and/ or
redistribute the software under the terms of the CeCILL license as circulated
by CEA, CNRS and INRIA at the following URL:
http://www.cecill.info/index.en.html
>
>As a counterpart to the access to the source code and  rights to copy, modify
and redistribute granted by the license, users are provided only with a limited
warranty  and the software's author,the holder of the economic rights, and the
successive licensors have only limited liability.
>
>In this respect, the user's attention is drawn to the risks associated with
loading, using, modifying and/or developing or reproducing the software by the
user in light of its specific status of free software, that may mean  that it
is complicated to manipulate, and that also therefore means  that it is
reserved for developers  and  experienced professionals having in-depth
computer knowledge. Users are therefore encouraged to load and test the
software's suitability as regards their requirements in conditions enabling
the security of their systems and/or data to be ensured and, more generally,
to use and operate it in the same conditions as regards security.
>
>The fact that you are presently reading this means that you have had knowledge
of the CeCILL license and that you accept its terms.


**Contributors**
--

| ![CNRS Logo](doc/Logo_cnrs.jpg) ![ENS Logo](doc/Logo_ens.jpg) |![LBMC Logo](doc/Logo_LBMC.jpg)|
|-----------------------------|------------|
|**CLUET David**|     [david.cluet@ens-lyon.fr](david.cluet@ens-lyon.fr)|


**Acknowledgments**
--

Under development


**Publication**
--

Under development



**User guide**
===

**1) Requirements**
-
The `LIPID_DROPLETS` macro requires `ImageJ v1.49g` or higher ([Download](https://imagej.nih.gov/ij/download.html)).

For ImageJ, the conversion of the analyzed stacks into animated GIFs requires the ([Gif-Stack-Writer Plugin](https://imagej.nih.gov/ij/plugins/gif-stack-writer.html)) that will be automatically installed, if missing.

To read the analysis report Markdown files, you can use the `Markdown Preview Plus` extension for `Chrome`. In the `Extension menu` allow access to files URL.



**2) INSTALLATION**
-
The `LIPID_DROPLETS` macro can be automatically installed with all required files in `ImageJ` and `FIJI`. Please follow the specific instructions described below.


![ImageJ Logo](doc/IJ.jpg)
-
1. Open `ImageJ`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation.ijm` file on the `ImageJ` Menu bar to open it.
4. In the Menu bar of the macro select the `Macros/Run Macro` option.
5. The window will be closed automatically and all required files will be installed in the `ImageJ/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `ImageJ` to refresh the Menu bar.


![FIJI Logo](doc/FIJI.jpg)
-
1. Open `FIJI`.
2. Open the `src` folder of the `LIPID_DROPLETS` macro.
3. Drag the `Installation_Fiji.ijm` file on `FIJI` Menu bar to open it.
4. In the console select the `Run` option.
5. All required files will be installed in the `Fiji.app/macros/Droplets` folder. The shortcut `Plugins/Macros/LIPID_DROPLETS` will be added in the Menu bar.
6. Restart `FIJI` to refresh the Menu bar.


**3) UPDATE**
-
Follow the same instructions as for the initial installation process.

**4) PERFORM AN ANALYSIS**
-

Click on the `Plugins/Macros/LIPID_DROPLETS` shortcut.

![Shortcut](doc/Shortcut.jpg)

The macro is initiated and the welcome window is prompted.

![Wecome](doc/Wecome.jpg)

The next window will propose different pre-set analysis modes:

![Modes](doc/Modes.jpg)

The settings are saved in the `settings.csv` file located in your `ImageJ/macros/Droplets/` folder.
They contain the key parameters for the analysis and are organized as following:

|Name|Lipid Droplets Brain|Lipid Droplets Retina|Repo Brain|
|----|--------------------|---------------------|----|
|Extension of the files to analyze|.czi|.czi|.czi|
|Reference resolution (micron/pixel) in X|0.156|0.156|0.156|
|Reference resolution (micron/pixel) in Y|0.156|0.156|0.156|
|Distance xy in pixels between 2 particles|5|5|5|
|Distance in z between 2 particles|5|5|5|
|Minimum size in pixel|7|7|164|
|Maximum size in pixel|15000|15000|15000|
|Maximum size (to exclude big fat bodies)|500|822|8400|
|Minimum circularity|0.5|0.5|0.3|
|Maximum circularity|1|1|1|
|Number of Iterations|3|2|3|
|Zone for enlargement (in pixel) and erasing|5|5|5|
|Number of bins for distributions|50|50|50|
|Zone of analysis|?|Manual ROI|Whole tissue|
|Minimal number of new Particles|50|50|5|
|Enhance signal|true|true|false|

If you respect this structure you can add your own settings. The various parameters will be described below. Once an analysis mode is selected you can modify all parameters using the main GUI.

![GUI](doc/GUI.jpg)

This interface displays all the options and parameters that will be used batchwise for the analysis of all your image .
In a parameters setting phase it could be usefull to be able to reuse always the same manual (optional) and slices selections. For this purpose, you can recall a previously determined `Manual ROI` and (starting, ending) slices couple, by selecting `YES` in the first listbox. If so you will be prompted later to select a previously created (by the macro) `*_Parameters.txt` file. All the new values of the key parameters modified in the interface will be applied.

Parameters
-

**Extension of the stacks files**: This correspond to the format of the files that will be manipulated (for example .czi, .tif, ...).

**Initial resolution used for the calibration**: The two following parameters correspond to the pixel value in micron of the set of stacks used to set your parameters values. Thus, in case of microscope change, and a subsequent different resolution, a correction coefficient will be applied.

**Region to process**: In order to provide versatile analyses the macro can manipulate the particles of interest can be detected and analyzed using one of the three following options:
- **Whole tissue with Sub-Selection**: The manipulator draws a `Region Of Interest` for each stack. During the automated analysis, the program detects the tissue using the ["Huang" thresholding method](https://pdfs.semanticscholar.org/8906/64d6e7861253bd8c36d0e9079f96c9f22d67.pdf) and will identify the particles of interest using [“Max-Entropy” threshold method](https://www.sciencedirect.com/science/article/pii/0734189X85901252). The particules of interest and then classified depending if they are located within the `Tissue`, specific of the `Region of Interest`, and within the `Tissue ` **but not in** `Region of Interest`.
- **Whole tissue**: During the automated analysis, the program detects the tissue and will identify the particles of interest present.   
- **Manual ROI**: The manipulator draws a `Region Of Interest` for each stack. During the automated analysis, the program detects the particles of interest present in this `Region of Interest`.   

**Thresholds between particles**: As this program is designed to perform analyses on stack images, it requires to remove duplicates of a same particle present on several slices. For this purpose the macro requires **minimal xy and z distances** to distinguish two separated particles. **Concerning the duplicates, only the largest one will be conserved.**

**Parameters of the initial low-resolution scan**: In order to precisely detect all particles of interest and their shape, the program iteratively enhance the **solidity** of the bright particles using the `Gaussian Blur...` and `Maximum...` treatments. The [“Max-Entropy” threshold method](https://www.sciencedirect.com/science/article/pii/0734189X85901252), then permits to detect the **particles of interest**. Thus at each iteration the program identify the brightest particles. They are stored in the `ROI manager` and removed from the image to allow the detection of less bright particles during the next iteration. This approach permits to precisely characterize the shape and size of the particles, but requires to remove any false positive bright particle. In this aim a first low resolution analysis detects even extremely big particles (**Maximal surface**) but with a minimal threshold (**Minimal surface**).

*Note that these two parameters are presented in microns in the GUI, but are expressed in pixels (as seen by the user, when performin visual inspection) in the `settings.csv` file.*

**Parameters for the high-resolution scan:** Once the potential particles of interest are identified, the program remove all of them that are not within the `Tissue` or that do not fit the searched properties:
- **Maximal surface** in microns (expressed in pixels in the `settings.csv`).
- **A specific shape** characterized by the **Minimal** and **Maximal [circularity](https://imagej.nih.gov/ij/docs/guide/146-30.html)**. *Note that this couple should be defined by values between 0 and 1.*

**Iteration parameters**: Depending on the heterogeneity of the labeling of your particles of interest, the **number of iterations** has to be optimized. In order to stop the analysis if this **maximal number of iterations** is too high, a **minimal number of new particles** is set. If the number of newly identified particles is below this value, the analysis stops. Depending on the noise of the labeling it is not enough to remove only the false positive and the discarded duplicates. It could be required to remove a larger zone that the identified shape. For this purpose an enlargement **correction factor** can be applied before the removal from the picture. Finally, the user can indicate the **number of bins** used to draw the distribution curves in the report and statistic files.

For some labeling with small and extremely bright noise, the **enhance signal** option can be unchecked. This will affect the research engine in two ways. First the `Maximum...` treatment will not be applied. Secondly, the **Minimal surface** filter will not be applied during the first iteration. This will allow the program to detect all **small noise particles** and remove them only at the second iteration. Indeed during the development phase, we encountered stacks that generated only **noise particles** during the first iteration, leading to the premature abortion of the program. The **non enhance signal** setting avoids this issue.

 Batch analysis
 -

 When all parameters are set the program prompts use to identify the **root folder** containing all your stacks.

![Folder](doc/Folder.jpg)

 The macro will analyze the folder and its sub-folder to identify all the files with the correct **extension**. the program then indicates how many files have been found.

![Number](doc/Number.jpg)

Channel Selection
-

The program will then load in background the first file. If you are using **hyperstacks** with several channels, the macro will ask you to specify in which channel the particles have to be found.

![Channels](doc/Channels.jpg)

Tailored stack analysis
-

Then the program will ask you **for each file** to select the **starting** and **ending** slices to be analyzed.
If the **Whole tissue with Sub-Selection** or **Manual ROI** options have been selected the program ask you to draw the **sub-selection** of interest.

![Selection](doc/Selection.jpg)

**These file specific parameters will be saved in a _Parameters.txt file within the root folder of your analysis, allowing later key parameters optimization.**
