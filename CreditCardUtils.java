package me.dashwith.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreditCardUtil {

  public static enum CardType {
    VISA, MASTERCARD, AMEX, DISCOVER, UNKNOWN;
  }

  public static final int CC_LEN_FOR_TYPE = 4;

  // See: http://www.regular-expressions.info/creditcard.html
  static final Pattern REGX_VISA = Pattern.compile("^4[0-9]{15}?");// VISA 16
  static final Pattern REGX_MC = Pattern.compile("^5[1-5][0-9]{14}$"); // MC 16
  static final Pattern REGX_AMEX = Pattern.compile("^3[47][0-9]{13}$");// AMEX 15
  static final Pattern REGX_DISCOVER = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$"); // Discover
  static final Pattern REGX_DINERS_CLUB = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]{11}$");
  // DinersClub

  static final Pattern TYPE_AMEX = Pattern.compile("^3[47][0-9]{2}$");// AMEX 15
  static final Pattern TYPE_VISA = Pattern.compile("^4[0-9]{3}?");// VISA 16
  static final Pattern TYPE_MC = Pattern.compile("^5[1-5][0-9]{2}$");// MC 16
  static final Pattern TYPE_DISCOVER = Pattern.compile("^6(?:011|5[0-9]{2})$"); // Discover
  static final Pattern TYPE_DINERS_CLUB = Pattern.compile("^3(?:0[0-5]|[68][0-9])[0-9]$");
  // DinersClub

  static String formatForViewing(CharSequence cc, CardType type) {
    String cleaned = cc.toString().replaceAll("[^\\d]", "");
    int origLen = cleaned.length();

    if (origLen <= CC_LEN_FOR_TYPE) { return cleaned; }

    StringBuilder builder = new StringBuilder(20);

    switch (type) {
      case VISA:
      case MASTERCARD:
      case DISCOVER: // { 4-4-4-4}
        if (origLen <= 4) {
          builder.append(cleaned);
          break;
        } else {
          builder.append(cleaned.substring(0, 4) + " ");
        }

        if (origLen <= 8) {
          builder.append(cleaned.substring(4));
          break;
        } else {
          builder.append(cleaned.substring(4, 8) + " ");
        }

        if (origLen <= 12) {
          builder.append(cleaned.substring(8));
          break;
        } else {
          builder.append(cleaned.substring(8, 12)).append(" ");
        }

        if (origLen <= 15) {
          builder.append(cleaned.substring(12));
          break;
        } else {
          builder.append(cleaned.substring(12, 16));
        }
        break;
      case AMEX: // {4-6-5}
        if (origLen <= 4) {
          builder.append(cleaned);
          break;
        } else {
          builder.append(cleaned.substring(0, 4)).append(" ");
        }

        if (origLen <= 10) {
          builder.append(cleaned.substring(4));
          break;
        } else {
          builder.append(cleaned.substring(4, 10)).append(" ");
        }

        if (origLen <= 15) {
          builder.append(cleaned.substring(10));
          break;
        } else {
          builder.append(cleaned.substring(10, 15));
        }
        break;
      default:
      case UNKNOWN:
        if (origLen <= 19) {
          builder.append(cleaned);
        } else {
          builder.append(cleaned.substring(0, 19));
        }
    }
    return builder.toString();
  }

  public static class CreditCardTextWatcher implements TextWatcher {

    private boolean changingText = false;
    private int cursorPos;
    private int editVelocity;
    private final WeakReference<EditText> editText;

    public CreditCardTextWatcher(EditText editText) {
      this.editText = new WeakReference<>(editText);
    }

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (this.changingText) { return; }
      this.editVelocity = (count - before);
      this.cursorPos = (start + count);
    }

    @Override public void afterTextChanged(Editable s) {
      if (changingText) { return; }
      changingText = true;
      CardType type;
      if (s.length() >= CC_LEN_FOR_TYPE) {
        type = findCardType(s.toString().replaceAll("[^\\d]", ""));
      } else {
        type = CardType.UNKNOWN;
      }
      String formatted = formatForViewing(s, type);
      s.replace(0, s.length(), formatted);

      int i = this.cursorPos;
      if (this.cursorPos >= formatted.length()) {
        this.cursorPos = formatted.length();
      }
      if ((this.editVelocity > 0)
          && (this.cursorPos > 0)
          && (formatted.charAt(-1 + this.cursorPos) == ' ')) {
        this.cursorPos += 1;
      }
      if ((this.editVelocity < 0)
          && (this.cursorPos > 1)
          && (formatted.charAt(-1 + this.cursorPos)
          == ' ')) {
        this.cursorPos -= 1;
      }
      EditText et = editText.get();
      if (this.cursorPos != i && et != null) {
        et.setSelection(this.cursorPos);
      }

      changingText = false;
    }

    static CardType findCardType(CharSequence s) {
      if (s.length() < CC_LEN_FOR_TYPE) { return CardType.UNKNOWN; }

      Pattern pattern = null;
      for (CardType cardType : CardType.values()) {
        switch (cardType) {
          case AMEX:
            pattern = TYPE_AMEX;
            break;
          case DISCOVER:
            pattern = TYPE_DISCOVER;
            break;
          case MASTERCARD:
            pattern = TYPE_MC;
            break;
          case VISA:
            pattern = TYPE_VISA;
            break;
          default:
            break;
        }
        Matcher matcher = pattern.matcher(s.subSequence(0, CC_LEN_FOR_TYPE));

        if (matcher.matches()) {
          return cardType;
        }
      }

      return CardType.UNKNOWN;
    }
  }
}
