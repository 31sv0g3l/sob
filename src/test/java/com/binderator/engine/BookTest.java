package com.binderator.engine;


import org.junit.*;
import java.util.*;


public class BookTest {

  private void testSignatureGeneration
  (int pageCount, int sheetsPerSignature, int[][][] expectedSignatures)
  {
    int[][][] signatures = Book.generateSignaturePageNumbers(pageCount, sheetsPerSignature, false);
    assert(Arrays.deepEquals(signatures, expectedSignatures));
  }

  @Test
  public void testMultipleSignatureGeneration
  ()
  throws Exception
  {
    {
      int pageCount = 20;
      int signatureSheets = 4;
      int [][][] signatures = {
        { {16, 1, 2, 15}, {14, 3, 4, 13}, {12, 5, 6, 11}, {10, 7, 8, 9} },
        { {0, 17, 18, 0}, {0, 19, 20, 0}, {0, 0, 0, 0}, {0, 0, 0, 0} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 7;
      int [][][] signatures = {
        { {28, 1, 2, 27}, {26, 3, 4, 25}, {24, 5, 6, 23}, {22, 7, 8, 21}, {20, 9, 10, 19}, {18, 11, 12, 17}, {16, 13, 14, 15} },
        { {56, 29, 30, 55}, {54, 31, 32, 53}, {52, 33, 34, 51}, {50, 35, 36, 49}, {48, 37, 38, 47}, {46, 39, 40, 45}, {44, 41, 42, 43} },
        { {84, 57, 58, 83}, {82, 59, 60, 81}, {80, 61, 62, 79}, {78, 63, 64, 77}, {76, 65, 66, 75}, {74, 67, 68, 73}, {72, 69, 70, 71} },
        { {0, 85, 86, 0}, {0, 87, 88, 0}, {0, 89, 90, 0}, {0, 91, 92, 0}, {0, 93, 94, 0}, {0, 95, 96, 0}, {100, 97, 98, 99} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 24;
      int [][][] signatures = {
        { {96, 1, 2, 95}, {94, 3, 4, 93}, {92, 5, 6, 91}, {90, 7, 8, 89}, {88, 9, 10, 87}, {86, 11, 12, 85}, {84, 13, 14, 83}, {82, 15, 16, 81}, {80, 17, 18, 79}, {78, 19, 20, 77}, {76, 21, 22, 75}, {74, 23, 24, 73}, {72, 25, 26, 71}, {70, 27, 28, 69}, {68, 29, 30, 67}, {66, 31, 32, 65}, {64, 33, 34, 63}, {62, 35, 36, 61}, {60, 37, 38, 59}, {58, 39, 40, 57}, {56, 41, 42, 55}, {54, 43, 44, 53}, {52, 45, 46, 51}, {50, 47, 48, 49} },
        { {0, 97, 98, 0}, {0, 99, 100, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 25;
      int [][][] signatures = {
        { {100, 1, 2, 99}, {98, 3, 4, 97}, {96, 5, 6, 95}, {94, 7, 8, 93}, {92, 9, 10, 91}, {90, 11, 12, 89}, {88, 13, 14, 87}, {86, 15, 16, 85}, {84, 17, 18, 83}, {82, 19, 20, 81}, {80, 21, 22, 79}, {78, 23, 24, 77}, {76, 25, 26, 75}, {74, 27, 28, 73}, {72, 29, 30, 71}, {70, 31, 32, 69}, {68, 33, 34, 67}, {66, 35, 36, 65}, {64, 37, 38, 63}, {62, 39, 40, 61}, {60, 41, 42, 59}, {58, 43, 44, 57}, {56, 45, 46, 55}, {54, 47, 48, 53}, {52, 49, 50, 51} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 26;
      int [][][] signatures = {
        { {0, 1, 2, 0}, {0, 3, 4, 0}, {100, 5, 6, 99}, {98, 7, 8, 97}, {96, 9, 10, 95}, {94, 11, 12, 93}, {92, 13, 14, 91}, {90, 15, 16, 89}, {88, 17, 18, 87}, {86, 19, 20, 85}, {84, 21, 22, 83}, {82, 23, 24, 81}, {80, 25, 26, 79}, {78, 27, 28, 77}, {76, 29, 30, 75}, {74, 31, 32, 73}, {72, 33, 34, 71}, {70, 35, 36, 69}, {68, 37, 38, 67}, {66, 39, 40, 65}, {64, 41, 42, 63}, {62, 43, 44, 61}, {60, 45, 46, 59}, {58, 47, 48, 57}, {56, 49, 50, 55}, {54, 51, 52, 53} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 2;
      int [][][] signatures = {
        { {8, 1, 2, 7}, {6, 3, 4, 5} },
        { {16, 9, 10, 15}, {14, 11, 12, 13} },
        { {24, 17, 18, 23}, {22, 19, 20, 21} },
        { {32, 25, 26, 31}, {30, 27, 28, 29} },
        { {40, 33, 34, 39}, {38, 35, 36, 37} },
        { {48, 41, 42, 47}, {46, 43, 44, 45} },
        { {56, 49, 50, 55}, {54, 51, 52, 53} },
        { {64, 57, 58, 63}, {62, 59, 60, 61} },
        { {72, 65, 66, 71}, {70, 67, 68, 69} },
        { {80, 73, 74, 79}, {78, 75, 76, 77} },
        { {88, 81, 82, 87}, {86, 83, 84, 85} },
        { {96, 89, 90, 95}, {94, 91, 92, 93} },
        { {0, 97, 98, 0}, {0, 99, 100, 0} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 100;
      int signatureSheets = 1;
      int [][][] signatures = {
        { {4, 1, 2, 3} },
        { {8, 5, 6, 7} },
        { {12, 9, 10, 11} },
        { {16, 13, 14, 15} },
        { {20, 17, 18, 19} },
        { {24, 21, 22, 23} },
        { {28, 25, 26, 27} },
        { {32, 29, 30, 31} },
        { {36, 33, 34, 35} },
        { {40, 37, 38, 39} },
        { {44, 41, 42, 43} },
        { {48, 45, 46, 47} },
        { {52, 49, 50, 51} },
        { {56, 53, 54, 55} },
        { {60, 57, 58, 59} },
        { {64, 61, 62, 63} },
        { {68, 65, 66, 67} },
        { {72, 69, 70, 71} },
        { {76, 73, 74, 75} },
        { {80, 77, 78, 79} },
        { {84, 81, 82, 83} },
        { {88, 85, 86, 87} },
        { {92, 89, 90, 91} },
        { {96, 93, 94, 95} },
        { {100, 97, 98, 99} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 1000;
      int signatureSheets = 3;
      int [][][] signatures = {
        { {12, 1, 2, 11}, {10, 3, 4, 9}, {8, 5, 6, 7} },
        { {24, 13, 14, 23}, {22, 15, 16, 21}, {20, 17, 18, 19} },
        { {36, 25, 26, 35}, {34, 27, 28, 33}, {32, 29, 30, 31} },
        { {48, 37, 38, 47}, {46, 39, 40, 45}, {44, 41, 42, 43} },
        { {60, 49, 50, 59}, {58, 51, 52, 57}, {56, 53, 54, 55} },
        { {72, 61, 62, 71}, {70, 63, 64, 69}, {68, 65, 66, 67} },
        { {84, 73, 74, 83}, {82, 75, 76, 81}, {80, 77, 78, 79} },
        { {96, 85, 86, 95}, {94, 87, 88, 93}, {92, 89, 90, 91} },
        { {108, 97, 98, 107}, {106, 99, 100, 105}, {104, 101, 102, 103} },
        { {120, 109, 110, 119}, {118, 111, 112, 117}, {116, 113, 114, 115} },
        { {132, 121, 122, 131}, {130, 123, 124, 129}, {128, 125, 126, 127} },
        { {144, 133, 134, 143}, {142, 135, 136, 141}, {140, 137, 138, 139} },
        { {156, 145, 146, 155}, {154, 147, 148, 153}, {152, 149, 150, 151} },
        { {168, 157, 158, 167}, {166, 159, 160, 165}, {164, 161, 162, 163} },
        { {180, 169, 170, 179}, {178, 171, 172, 177}, {176, 173, 174, 175} },
        { {192, 181, 182, 191}, {190, 183, 184, 189}, {188, 185, 186, 187} },
        { {204, 193, 194, 203}, {202, 195, 196, 201}, {200, 197, 198, 199} },
        { {216, 205, 206, 215}, {214, 207, 208, 213}, {212, 209, 210, 211} },
        { {228, 217, 218, 227}, {226, 219, 220, 225}, {224, 221, 222, 223} },
        { {240, 229, 230, 239}, {238, 231, 232, 237}, {236, 233, 234, 235} },
        { {252, 241, 242, 251}, {250, 243, 244, 249}, {248, 245, 246, 247} },
        { {264, 253, 254, 263}, {262, 255, 256, 261}, {260, 257, 258, 259} },
        { {276, 265, 266, 275}, {274, 267, 268, 273}, {272, 269, 270, 271} },
        { {288, 277, 278, 287}, {286, 279, 280, 285}, {284, 281, 282, 283} },
        { {300, 289, 290, 299}, {298, 291, 292, 297}, {296, 293, 294, 295} },
        { {312, 301, 302, 311}, {310, 303, 304, 309}, {308, 305, 306, 307} },
        { {324, 313, 314, 323}, {322, 315, 316, 321}, {320, 317, 318, 319} },
        { {336, 325, 326, 335}, {334, 327, 328, 333}, {332, 329, 330, 331} },
        { {348, 337, 338, 347}, {346, 339, 340, 345}, {344, 341, 342, 343} },
        { {360, 349, 350, 359}, {358, 351, 352, 357}, {356, 353, 354, 355} },
        { {372, 361, 362, 371}, {370, 363, 364, 369}, {368, 365, 366, 367} },
        { {384, 373, 374, 383}, {382, 375, 376, 381}, {380, 377, 378, 379} },
        { {396, 385, 386, 395}, {394, 387, 388, 393}, {392, 389, 390, 391} },
        { {408, 397, 398, 407}, {406, 399, 400, 405}, {404, 401, 402, 403} },
        { {420, 409, 410, 419}, {418, 411, 412, 417}, {416, 413, 414, 415} },
        { {432, 421, 422, 431}, {430, 423, 424, 429}, {428, 425, 426, 427} },
        { {444, 433, 434, 443}, {442, 435, 436, 441}, {440, 437, 438, 439} },
        { {456, 445, 446, 455}, {454, 447, 448, 453}, {452, 449, 450, 451} },
        { {468, 457, 458, 467}, {466, 459, 460, 465}, {464, 461, 462, 463} },
        { {480, 469, 470, 479}, {478, 471, 472, 477}, {476, 473, 474, 475} },
        { {492, 481, 482, 491}, {490, 483, 484, 489}, {488, 485, 486, 487} },
        { {504, 493, 494, 503}, {502, 495, 496, 501}, {500, 497, 498, 499} },
        { {516, 505, 506, 515}, {514, 507, 508, 513}, {512, 509, 510, 511} },
        { {528, 517, 518, 527}, {526, 519, 520, 525}, {524, 521, 522, 523} },
        { {540, 529, 530, 539}, {538, 531, 532, 537}, {536, 533, 534, 535} },
        { {552, 541, 542, 551}, {550, 543, 544, 549}, {548, 545, 546, 547} },
        { {564, 553, 554, 563}, {562, 555, 556, 561}, {560, 557, 558, 559} },
        { {576, 565, 566, 575}, {574, 567, 568, 573}, {572, 569, 570, 571} },
        { {588, 577, 578, 587}, {586, 579, 580, 585}, {584, 581, 582, 583} },
        { {600, 589, 590, 599}, {598, 591, 592, 597}, {596, 593, 594, 595} },
        { {612, 601, 602, 611}, {610, 603, 604, 609}, {608, 605, 606, 607} },
        { {624, 613, 614, 623}, {622, 615, 616, 621}, {620, 617, 618, 619} },
        { {636, 625, 626, 635}, {634, 627, 628, 633}, {632, 629, 630, 631} },
        { {648, 637, 638, 647}, {646, 639, 640, 645}, {644, 641, 642, 643} },
        { {660, 649, 650, 659}, {658, 651, 652, 657}, {656, 653, 654, 655} },
        { {672, 661, 662, 671}, {670, 663, 664, 669}, {668, 665, 666, 667} },
        { {684, 673, 674, 683}, {682, 675, 676, 681}, {680, 677, 678, 679} },
        { {696, 685, 686, 695}, {694, 687, 688, 693}, {692, 689, 690, 691} },
        { {708, 697, 698, 707}, {706, 699, 700, 705}, {704, 701, 702, 703} },
        { {720, 709, 710, 719}, {718, 711, 712, 717}, {716, 713, 714, 715} },
        { {732, 721, 722, 731}, {730, 723, 724, 729}, {728, 725, 726, 727} },
        { {744, 733, 734, 743}, {742, 735, 736, 741}, {740, 737, 738, 739} },
        { {756, 745, 746, 755}, {754, 747, 748, 753}, {752, 749, 750, 751} },
        { {768, 757, 758, 767}, {766, 759, 760, 765}, {764, 761, 762, 763} },
        { {780, 769, 770, 779}, {778, 771, 772, 777}, {776, 773, 774, 775} },
        { {792, 781, 782, 791}, {790, 783, 784, 789}, {788, 785, 786, 787} },
        { {804, 793, 794, 803}, {802, 795, 796, 801}, {800, 797, 798, 799} },
        { {816, 805, 806, 815}, {814, 807, 808, 813}, {812, 809, 810, 811} },
        { {828, 817, 818, 827}, {826, 819, 820, 825}, {824, 821, 822, 823} },
        { {840, 829, 830, 839}, {838, 831, 832, 837}, {836, 833, 834, 835} },
        { {852, 841, 842, 851}, {850, 843, 844, 849}, {848, 845, 846, 847} },
        { {864, 853, 854, 863}, {862, 855, 856, 861}, {860, 857, 858, 859} },
        { {876, 865, 866, 875}, {874, 867, 868, 873}, {872, 869, 870, 871} },
        { {888, 877, 878, 887}, {886, 879, 880, 885}, {884, 881, 882, 883} },
        { {900, 889, 890, 899}, {898, 891, 892, 897}, {896, 893, 894, 895} },
        { {912, 901, 902, 911}, {910, 903, 904, 909}, {908, 905, 906, 907} },
        { {924, 913, 914, 923}, {922, 915, 916, 921}, {920, 917, 918, 919} },
        { {936, 925, 926, 935}, {934, 927, 928, 933}, {932, 929, 930, 931} },
        { {948, 937, 938, 947}, {946, 939, 940, 945}, {944, 941, 942, 943} },
        { {960, 949, 950, 959}, {958, 951, 952, 957}, {956, 953, 954, 955} },
        { {972, 961, 962, 971}, {970, 963, 964, 969}, {968, 965, 966, 967} },
        { {984, 973, 974, 983}, {982, 975, 976, 981}, {980, 977, 978, 979} },
        { {996, 985, 986, 995}, {994, 987, 988, 993}, {992, 989, 990, 991} },
        { {0, 997, 998, 0}, {0, 999, 1000, 0}, {0, 0, 0, 0} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 1000;
      int signatureSheets = 6;
      int [][][] signatures = {
        { {24, 1, 2, 23}, {22, 3, 4, 21}, {20, 5, 6, 19}, {18, 7, 8, 17}, {16, 9, 10, 15}, {14, 11, 12, 13} },
        { {48, 25, 26, 47}, {46, 27, 28, 45}, {44, 29, 30, 43}, {42, 31, 32, 41}, {40, 33, 34, 39}, {38, 35, 36, 37} },
        { {72, 49, 50, 71}, {70, 51, 52, 69}, {68, 53, 54, 67}, {66, 55, 56, 65}, {64, 57, 58, 63}, {62, 59, 60, 61} },
        { {96, 73, 74, 95}, {94, 75, 76, 93}, {92, 77, 78, 91}, {90, 79, 80, 89}, {88, 81, 82, 87}, {86, 83, 84, 85} },
        { {120, 97, 98, 119}, {118, 99, 100, 117}, {116, 101, 102, 115}, {114, 103, 104, 113}, {112, 105, 106, 111}, {110, 107, 108, 109} },
        { {144, 121, 122, 143}, {142, 123, 124, 141}, {140, 125, 126, 139}, {138, 127, 128, 137}, {136, 129, 130, 135}, {134, 131, 132, 133} },
        { {168, 145, 146, 167}, {166, 147, 148, 165}, {164, 149, 150, 163}, {162, 151, 152, 161}, {160, 153, 154, 159}, {158, 155, 156, 157} },
        { {192, 169, 170, 191}, {190, 171, 172, 189}, {188, 173, 174, 187}, {186, 175, 176, 185}, {184, 177, 178, 183}, {182, 179, 180, 181} },
        { {216, 193, 194, 215}, {214, 195, 196, 213}, {212, 197, 198, 211}, {210, 199, 200, 209}, {208, 201, 202, 207}, {206, 203, 204, 205} },
        { {240, 217, 218, 239}, {238, 219, 220, 237}, {236, 221, 222, 235}, {234, 223, 224, 233}, {232, 225, 226, 231}, {230, 227, 228, 229} },
        { {264, 241, 242, 263}, {262, 243, 244, 261}, {260, 245, 246, 259}, {258, 247, 248, 257}, {256, 249, 250, 255}, {254, 251, 252, 253} },
        { {288, 265, 266, 287}, {286, 267, 268, 285}, {284, 269, 270, 283}, {282, 271, 272, 281}, {280, 273, 274, 279}, {278, 275, 276, 277} },
        { {312, 289, 290, 311}, {310, 291, 292, 309}, {308, 293, 294, 307}, {306, 295, 296, 305}, {304, 297, 298, 303}, {302, 299, 300, 301} },
        { {336, 313, 314, 335}, {334, 315, 316, 333}, {332, 317, 318, 331}, {330, 319, 320, 329}, {328, 321, 322, 327}, {326, 323, 324, 325} },
        { {360, 337, 338, 359}, {358, 339, 340, 357}, {356, 341, 342, 355}, {354, 343, 344, 353}, {352, 345, 346, 351}, {350, 347, 348, 349} },
        { {384, 361, 362, 383}, {382, 363, 364, 381}, {380, 365, 366, 379}, {378, 367, 368, 377}, {376, 369, 370, 375}, {374, 371, 372, 373} },
        { {408, 385, 386, 407}, {406, 387, 388, 405}, {404, 389, 390, 403}, {402, 391, 392, 401}, {400, 393, 394, 399}, {398, 395, 396, 397} },
        { {432, 409, 410, 431}, {430, 411, 412, 429}, {428, 413, 414, 427}, {426, 415, 416, 425}, {424, 417, 418, 423}, {422, 419, 420, 421} },
        { {456, 433, 434, 455}, {454, 435, 436, 453}, {452, 437, 438, 451}, {450, 439, 440, 449}, {448, 441, 442, 447}, {446, 443, 444, 445} },
        { {480, 457, 458, 479}, {478, 459, 460, 477}, {476, 461, 462, 475}, {474, 463, 464, 473}, {472, 465, 466, 471}, {470, 467, 468, 469} },
        { {504, 481, 482, 503}, {502, 483, 484, 501}, {500, 485, 486, 499}, {498, 487, 488, 497}, {496, 489, 490, 495}, {494, 491, 492, 493} },
        { {528, 505, 506, 527}, {526, 507, 508, 525}, {524, 509, 510, 523}, {522, 511, 512, 521}, {520, 513, 514, 519}, {518, 515, 516, 517} },
        { {552, 529, 530, 551}, {550, 531, 532, 549}, {548, 533, 534, 547}, {546, 535, 536, 545}, {544, 537, 538, 543}, {542, 539, 540, 541} },
        { {576, 553, 554, 575}, {574, 555, 556, 573}, {572, 557, 558, 571}, {570, 559, 560, 569}, {568, 561, 562, 567}, {566, 563, 564, 565} },
        { {600, 577, 578, 599}, {598, 579, 580, 597}, {596, 581, 582, 595}, {594, 583, 584, 593}, {592, 585, 586, 591}, {590, 587, 588, 589} },
        { {624, 601, 602, 623}, {622, 603, 604, 621}, {620, 605, 606, 619}, {618, 607, 608, 617}, {616, 609, 610, 615}, {614, 611, 612, 613} },
        { {648, 625, 626, 647}, {646, 627, 628, 645}, {644, 629, 630, 643}, {642, 631, 632, 641}, {640, 633, 634, 639}, {638, 635, 636, 637} },
        { {672, 649, 650, 671}, {670, 651, 652, 669}, {668, 653, 654, 667}, {666, 655, 656, 665}, {664, 657, 658, 663}, {662, 659, 660, 661} },
        { {696, 673, 674, 695}, {694, 675, 676, 693}, {692, 677, 678, 691}, {690, 679, 680, 689}, {688, 681, 682, 687}, {686, 683, 684, 685} },
        { {720, 697, 698, 719}, {718, 699, 700, 717}, {716, 701, 702, 715}, {714, 703, 704, 713}, {712, 705, 706, 711}, {710, 707, 708, 709} },
        { {744, 721, 722, 743}, {742, 723, 724, 741}, {740, 725, 726, 739}, {738, 727, 728, 737}, {736, 729, 730, 735}, {734, 731, 732, 733} },
        { {768, 745, 746, 767}, {766, 747, 748, 765}, {764, 749, 750, 763}, {762, 751, 752, 761}, {760, 753, 754, 759}, {758, 755, 756, 757} },
        { {792, 769, 770, 791}, {790, 771, 772, 789}, {788, 773, 774, 787}, {786, 775, 776, 785}, {784, 777, 778, 783}, {782, 779, 780, 781} },
        { {816, 793, 794, 815}, {814, 795, 796, 813}, {812, 797, 798, 811}, {810, 799, 800, 809}, {808, 801, 802, 807}, {806, 803, 804, 805} },
        { {840, 817, 818, 839}, {838, 819, 820, 837}, {836, 821, 822, 835}, {834, 823, 824, 833}, {832, 825, 826, 831}, {830, 827, 828, 829} },
        { {864, 841, 842, 863}, {862, 843, 844, 861}, {860, 845, 846, 859}, {858, 847, 848, 857}, {856, 849, 850, 855}, {854, 851, 852, 853} },
        { {888, 865, 866, 887}, {886, 867, 868, 885}, {884, 869, 870, 883}, {882, 871, 872, 881}, {880, 873, 874, 879}, {878, 875, 876, 877} },
        { {912, 889, 890, 911}, {910, 891, 892, 909}, {908, 893, 894, 907}, {906, 895, 896, 905}, {904, 897, 898, 903}, {902, 899, 900, 901} },
        { {936, 913, 914, 935}, {934, 915, 916, 933}, {932, 917, 918, 931}, {930, 919, 920, 929}, {928, 921, 922, 927}, {926, 923, 924, 925} },
        { {960, 937, 938, 959}, {958, 939, 940, 957}, {956, 941, 942, 955}, {954, 943, 944, 953}, {952, 945, 946, 951}, {950, 947, 948, 949} },
        { {984, 961, 962, 983}, {982, 963, 964, 981}, {980, 965, 966, 979}, {978, 967, 968, 977}, {976, 969, 970, 975}, {974, 971, 972, 973} },
        { {0, 985, 986, 0}, {0, 987, 988, 0}, {0, 989, 990, 0}, {0, 991, 992, 0}, {1000, 993, 994, 999}, {998, 995, 996, 997} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
    {
      int pageCount = 1000;
      int signatureSheets = 8;
      int [][][] signatures = {
        { {32, 1, 2, 31}, {30, 3, 4, 29}, {28, 5, 6, 27}, {26, 7, 8, 25}, {24, 9, 10, 23}, {22, 11, 12, 21}, {20, 13, 14, 19}, {18, 15, 16, 17} },
        { {64, 33, 34, 63}, {62, 35, 36, 61}, {60, 37, 38, 59}, {58, 39, 40, 57}, {56, 41, 42, 55}, {54, 43, 44, 53}, {52, 45, 46, 51}, {50, 47, 48, 49} },
        { {96, 65, 66, 95}, {94, 67, 68, 93}, {92, 69, 70, 91}, {90, 71, 72, 89}, {88, 73, 74, 87}, {86, 75, 76, 85}, {84, 77, 78, 83}, {82, 79, 80, 81} },
        { {128, 97, 98, 127}, {126, 99, 100, 125}, {124, 101, 102, 123}, {122, 103, 104, 121}, {120, 105, 106, 119}, {118, 107, 108, 117}, {116, 109, 110, 115}, {114, 111, 112, 113} },
        { {160, 129, 130, 159}, {158, 131, 132, 157}, {156, 133, 134, 155}, {154, 135, 136, 153}, {152, 137, 138, 151}, {150, 139, 140, 149}, {148, 141, 142, 147}, {146, 143, 144, 145} },
        { {192, 161, 162, 191}, {190, 163, 164, 189}, {188, 165, 166, 187}, {186, 167, 168, 185}, {184, 169, 170, 183}, {182, 171, 172, 181}, {180, 173, 174, 179}, {178, 175, 176, 177} },
        { {224, 193, 194, 223}, {222, 195, 196, 221}, {220, 197, 198, 219}, {218, 199, 200, 217}, {216, 201, 202, 215}, {214, 203, 204, 213}, {212, 205, 206, 211}, {210, 207, 208, 209} },
        { {256, 225, 226, 255}, {254, 227, 228, 253}, {252, 229, 230, 251}, {250, 231, 232, 249}, {248, 233, 234, 247}, {246, 235, 236, 245}, {244, 237, 238, 243}, {242, 239, 240, 241} },
        { {288, 257, 258, 287}, {286, 259, 260, 285}, {284, 261, 262, 283}, {282, 263, 264, 281}, {280, 265, 266, 279}, {278, 267, 268, 277}, {276, 269, 270, 275}, {274, 271, 272, 273} },
        { {320, 289, 290, 319}, {318, 291, 292, 317}, {316, 293, 294, 315}, {314, 295, 296, 313}, {312, 297, 298, 311}, {310, 299, 300, 309}, {308, 301, 302, 307}, {306, 303, 304, 305} },
        { {352, 321, 322, 351}, {350, 323, 324, 349}, {348, 325, 326, 347}, {346, 327, 328, 345}, {344, 329, 330, 343}, {342, 331, 332, 341}, {340, 333, 334, 339}, {338, 335, 336, 337} },
        { {384, 353, 354, 383}, {382, 355, 356, 381}, {380, 357, 358, 379}, {378, 359, 360, 377}, {376, 361, 362, 375}, {374, 363, 364, 373}, {372, 365, 366, 371}, {370, 367, 368, 369} },
        { {416, 385, 386, 415}, {414, 387, 388, 413}, {412, 389, 390, 411}, {410, 391, 392, 409}, {408, 393, 394, 407}, {406, 395, 396, 405}, {404, 397, 398, 403}, {402, 399, 400, 401} },
        { {448, 417, 418, 447}, {446, 419, 420, 445}, {444, 421, 422, 443}, {442, 423, 424, 441}, {440, 425, 426, 439}, {438, 427, 428, 437}, {436, 429, 430, 435}, {434, 431, 432, 433} },
        { {480, 449, 450, 479}, {478, 451, 452, 477}, {476, 453, 454, 475}, {474, 455, 456, 473}, {472, 457, 458, 471}, {470, 459, 460, 469}, {468, 461, 462, 467}, {466, 463, 464, 465} },
        { {512, 481, 482, 511}, {510, 483, 484, 509}, {508, 485, 486, 507}, {506, 487, 488, 505}, {504, 489, 490, 503}, {502, 491, 492, 501}, {500, 493, 494, 499}, {498, 495, 496, 497} },
        { {544, 513, 514, 543}, {542, 515, 516, 541}, {540, 517, 518, 539}, {538, 519, 520, 537}, {536, 521, 522, 535}, {534, 523, 524, 533}, {532, 525, 526, 531}, {530, 527, 528, 529} },
        { {576, 545, 546, 575}, {574, 547, 548, 573}, {572, 549, 550, 571}, {570, 551, 552, 569}, {568, 553, 554, 567}, {566, 555, 556, 565}, {564, 557, 558, 563}, {562, 559, 560, 561} },
        { {608, 577, 578, 607}, {606, 579, 580, 605}, {604, 581, 582, 603}, {602, 583, 584, 601}, {600, 585, 586, 599}, {598, 587, 588, 597}, {596, 589, 590, 595}, {594, 591, 592, 593} },
        { {640, 609, 610, 639}, {638, 611, 612, 637}, {636, 613, 614, 635}, {634, 615, 616, 633}, {632, 617, 618, 631}, {630, 619, 620, 629}, {628, 621, 622, 627}, {626, 623, 624, 625} },
        { {672, 641, 642, 671}, {670, 643, 644, 669}, {668, 645, 646, 667}, {666, 647, 648, 665}, {664, 649, 650, 663}, {662, 651, 652, 661}, {660, 653, 654, 659}, {658, 655, 656, 657} },
        { {704, 673, 674, 703}, {702, 675, 676, 701}, {700, 677, 678, 699}, {698, 679, 680, 697}, {696, 681, 682, 695}, {694, 683, 684, 693}, {692, 685, 686, 691}, {690, 687, 688, 689} },
        { {736, 705, 706, 735}, {734, 707, 708, 733}, {732, 709, 710, 731}, {730, 711, 712, 729}, {728, 713, 714, 727}, {726, 715, 716, 725}, {724, 717, 718, 723}, {722, 719, 720, 721} },
        { {768, 737, 738, 767}, {766, 739, 740, 765}, {764, 741, 742, 763}, {762, 743, 744, 761}, {760, 745, 746, 759}, {758, 747, 748, 757}, {756, 749, 750, 755}, {754, 751, 752, 753} },
        { {800, 769, 770, 799}, {798, 771, 772, 797}, {796, 773, 774, 795}, {794, 775, 776, 793}, {792, 777, 778, 791}, {790, 779, 780, 789}, {788, 781, 782, 787}, {786, 783, 784, 785} },
        { {832, 801, 802, 831}, {830, 803, 804, 829}, {828, 805, 806, 827}, {826, 807, 808, 825}, {824, 809, 810, 823}, {822, 811, 812, 821}, {820, 813, 814, 819}, {818, 815, 816, 817} },
        { {864, 833, 834, 863}, {862, 835, 836, 861}, {860, 837, 838, 859}, {858, 839, 840, 857}, {856, 841, 842, 855}, {854, 843, 844, 853}, {852, 845, 846, 851}, {850, 847, 848, 849} },
        { {896, 865, 866, 895}, {894, 867, 868, 893}, {892, 869, 870, 891}, {890, 871, 872, 889}, {888, 873, 874, 887}, {886, 875, 876, 885}, {884, 877, 878, 883}, {882, 879, 880, 881} },
        { {928, 897, 898, 927}, {926, 899, 900, 925}, {924, 901, 902, 923}, {922, 903, 904, 921}, {920, 905, 906, 919}, {918, 907, 908, 917}, {916, 909, 910, 915}, {914, 911, 912, 913} },
        { {960, 929, 930, 959}, {958, 931, 932, 957}, {956, 933, 934, 955}, {954, 935, 936, 953}, {952, 937, 938, 951}, {950, 939, 940, 949}, {948, 941, 942, 947}, {946, 943, 944, 945} },
        { {992, 961, 962, 991}, {990, 963, 964, 989}, {988, 965, 966, 987}, {986, 967, 968, 985}, {984, 969, 970, 983}, {982, 971, 972, 981}, {980, 973, 974, 979}, {978, 975, 976, 977} },
        { {0, 993, 994, 0}, {0, 995, 996, 0}, {0, 997, 998, 0}, {0, 999, 1000, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0} }
      };
      testSignatureGeneration(pageCount, signatureSheets, signatures);
    }
  }

  public static void generateSignatureTest
  (int pageCount, int sheetsPerSignature)
  {
    int[][][] signatures = Book.generateSignaturePageNumbers(pageCount, sheetsPerSignature, false);
    int signatureCount = 0;
    System.err.print("{\n");
    System.err.print("int pageCount = " + pageCount + ";\n");
    System.err.print("int sheetsPerSignature = " + sheetsPerSignature + ";\n");
    System.err.print("int [][][] signatures = {\n");
    for (int[][] signature : signatures) {
      if (signatureCount++ > 0) {
        System.err.print(",\n");
      }
      System.err.print("  { ");
      int signatureSheetCount = 0;
      for (int[] signatureSheet : signature) {
        if (signatureSheetCount++ > 0) {
          System.err.print(", ");
        }
        System.err.print("{");
        for (int i = 0; i < 4; i++) {
          if (i > 0) {
            System.err.print(", ");
          }
          System.err.print(signatureSheet[i]);
        }
        System.err.print("}");
      }
      System.err.print(" }");
    }
    System.err.print("\n};\ntestSignatureGeneration(pageCount, sheetsPerSignature, signatures);\n}\n");
  }

  public static void main
  (String[] args)
  {
    if (args.length < 2) {
      System.err.println("Need two args: pageCount and sheetsPerSignature");
      System.exit(1);
    }
    int pageCount = Integer.parseInt(args[0]);
    int pagesPerSignature = Integer.parseInt(args[1]);
    generateSignatureTest(pageCount, pagesPerSignature);
  }

}
