# Son of Binderator

![Model](src/main/resources/help/images/binderator_preview.png)

Son of Binderator (SOB) is a Java-based graphical tool for bookbinding, specifically for cleaning up
PDFs into a form suitable for printing, either as a simple output document or as
foldable / stitchable binding signatures (sections). Arbitrary page ranges of source PDFs can be
combined and transformed  with scaling, rotation, various forms of cropping, and horizontal
and vertical translation.

An aribtrary combination of transformations can be applied to any page or set of pages, with
cumulative effect.  An inline (ICE)PDF viewer can be used to see the effects of transformations
live as transform slider controls are used to change values, eliminating the need to regenerate
an output PDF with every change for an external viewer, although this is still possible.
Bookbinding signatures can be generated with variable spine and edge offsets, to a variety of
page sizes.

SOB has been tested on Windows, Linux and Mac environments and
is actively developed on an Apple Silicon machine.

To install and run,
<a href="https://www.oracle.com/au/java/technologies/downloads/">download and install Java 19</a>
(or a later version if you prefer) from Oracle and then either
<a href="https://github.com/31sv0g3l/sob/releases">download a runnable release jar</a>
and double click on that, or check out the source and build the maven package target to produce
your own runnable jar.

All documentation is currently inline under the Help menu in the running application, but will be
copied to this page in the future.
