package com.binderator.engine;

import com.binderator.util.*;

import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public class Transform implements Serializable {

  @Serial
  private static final long serialVersionUID = 5223516576057447087L;

  public enum Type {
    ROTATION_IN_DEGREES, SCALE, SCALE_X, SCALE_Y, LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP,
    X_TRANSLATION, Y_TRANSLATION;

    private final String displayString;

    Type
    ()
    {
      displayString = Translations.translate(name());
    }

    @Override
    public String toString
    ()
    {
      return displayString;
    }

  }

  public enum GeneralType { ROTATION, SCALE, CROP, TRANSLATION }

  Integer id = null;
  Type type;
  RangedFloat value;

  public Transform
  (Type type, float value, float lowValue, float highValue)
  {
    this.type = type;
    this.value = new RangedFloat(type.name(), value, lowValue, highValue);
  }

  public Transform
  (int id, int type, float value, float lowValue, float highValue)
  throws Exception
  {
    this.id = id;
    if ((type < 0) || (type > Type.values().length - 1)) {
      throw new Exception("Invalid ordinal " + type + " for transform type");
    }
    this.type = Type.values()[type];
    this.value = new RangedFloat(this.type.name(), value, lowValue, highValue);
  }

  public static GeneralType getGeneralType
  (Type type)
  {
    return switch (type) {
      case ROTATION_IN_DEGREES -> GeneralType.ROTATION;
      case SCALE, SCALE_X, SCALE_Y -> GeneralType.SCALE;
      case LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP -> GeneralType.CROP;
      case X_TRANSLATION, Y_TRANSLATION -> GeneralType.TRANSLATION;
    };
  }

  public Integer getId
  ()
  {
    return id;
  }

  public void setId
  (int id)
  {
    this.id = id;
  }

  public Type getType
  ()
  {
    return type;
  }

  public RangedFloat getRangedFloat
  ()
  {
    return value;
  }

  public String toString
  ()
  {
    switch (type) {
      case ROTATION_IN_DEGREES -> {
        return "Counterclockwise Rotation in Degrees";
      }
      case SCALE -> {
        return "Scale Ratio";
      }
      case SCALE_X -> {
        return "Horizontal Scale Ratio";
      }
      case SCALE_Y -> {
        return "Vertical Scale Ratio";
      }
      case LEFT_CROP -> {
        return "Left Crop Ratio";
      }
      case RIGHT_CROP -> {
        return "Right Crop Ratio";
      }
      case BOTTOM_CROP -> {
        return "Bottom Crop Ratio";
      }
      case TOP_CROP -> {
        return "Top Crop Ratio";
      }
      case X_TRANSLATION -> {
        return "Horizontal Translation Ratio";
      }
      case Y_TRANSLATION -> {
        return "Vertical Translation Ratio";
      }
    }
    return "";
  }

  public static float getDefaultValue
    (Type type)
  {
    return switch (type) {
      case SCALE, SCALE_X, SCALE_Y -> 1.0f;
      default -> 0.0f;
    };
  }

  public static float getDefaultLowValue
  (Type type)
  {
    return switch (type) {
      case ROTATION_IN_DEGREES -> -10.0f;
      case SCALE, SCALE_X, SCALE_Y -> 0.5f;
      case LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP -> 0.0f;
      case X_TRANSLATION, Y_TRANSLATION -> -0.10f;
    };
  }

  public static float getDefaultHighValue
  (Type type)
  {
    return switch (type) {
      case ROTATION_IN_DEGREES -> 10.0f;
      case SCALE, SCALE_X, SCALE_Y -> 1.5f;
      case LEFT_CROP, RIGHT_CROP, BOTTOM_CROP, TOP_CROP -> 0.1f;
      case X_TRANSLATION, Y_TRANSLATION -> 0.10f;
    };
  }

}
