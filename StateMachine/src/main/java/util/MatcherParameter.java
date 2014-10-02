package util;

import java.util.Arrays;
import java.util.regex.Matcher;

public class MatcherParameter {
  private final String[] result;

  public MatcherParameter(Matcher matcher) {
    result = new String[matcher.groupCount() + 1];
    for (int i = 0; i <= matcher.groupCount(); i++) {
      result[i] = matcher.group(i);
    }
  }
  
  public MatcherParameter(String word) {
    result = new String[] {word};
  }
  
  /**
   * Returns the number of capturing groups in this matcher's pattern.
   *
   * <p> Group zero denotes the entire pattern by convention. It is not
   * included in this count.
   *
   * <p> Any non-negative integer smaller than or equal to the value
   * returned by this method is guaranteed to be a valid group index for
   * this matcher.  </p>
   *
   * @return The number of capturing groups in this matcher's pattern
   */
  public int groupCount() {
    return result.length - 1;
  }
  
  /**
   * Returns the input subsequence captured by the given group during the
   * previous match operation.
   *
   * @param  group
   *         The index of a capturing group in this matcher's pattern
   *
   * @return  The (possibly empty) subsequence captured by the group
   *          during the previous match, or <tt>null</tt> if the group
   *          failed to match part of the input
   *
   * @throws  IllegalStateException
   *          If no match has yet been attempted,
   *          or if the previous match operation failed
   *
   * @throws  IndexOutOfBoundsException
   *          If there is no capturing group in the pattern
   *          with the given index
   */
  public String group(int i) {
    return result[i];
  }
  
  /**
   * Returns the input subsequence matched by the previous match.
   *
   * <p> For a matcher <i>m</i> with input sequence <i>s</i>,
   * the expressions <i>m.</i><tt>group()</tt> and
   * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
   * are equivalent.  </p>
   *
   * <p> Note that some patterns, for example <tt>a*</tt>, match the empty
   * string.  This method will return the empty string when the pattern
   * successfully matches the empty string in the input.  </p>
   *
   * @return The (possibly empty) subsequence matched by the previous match,
   *         in string form
   *
   * @throws  IllegalStateException
   *          If no match has yet been attempted,
   *          or if the previous match operation failed
   */
  public String group() {
    return result[0];
  }
  
  @Override
  public String toString() {
    return Arrays.toString(result);
  }
}