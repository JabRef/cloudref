import { AbstractControl } from '@angular/forms';

export class BracesValidator {

  static validate(c: AbstractControl) {

    return BracesValidator.checkBraces(c.value) ? null : {
      validateBraces: {
        valid: false,
      },
    };
  }

  /**
   * Function adopted from:
   * https://github.com/JabRef/jabref/blob/723af6dddfb4339e5f5fec3bb38869344201c287/src/main/java/org/jabref/logic/bibtex/LatexFieldFormatter.java#L42
   * https://stackoverflow.com/a/16874430/8446649
   *
   * @param value content of the input field.
   * @returns {boolean} true if braces match, false otherwise.
   */
  private static checkBraces(value: any) {
    let arr = [];
    value = value.toString();

    for (let i = 0; i < value.length; i++) {
      let item: string = value.charAt(i);

      let charBeforeIsEscape: boolean = false;
      if (i > 0 && value.charAt(i - 1) === '\\') {
        charBeforeIsEscape = true;
      }

      if (!charBeforeIsEscape && item === '{') {
        arr.push(item);
      } else if (!charBeforeIsEscape && item === '}') {
        if (arr.length === 0) {
          // no matching brace
          return false;
        } else {
          // remove matching brace from array
          arr.pop();
        }
      }
    }

    // check if all braces are closed
    if (arr.length === 0) {
      return true;
    } else {
      return false;
    }
  }
}
