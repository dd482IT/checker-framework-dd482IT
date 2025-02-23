package org.checkerframework.common.subtyping;

import java.lang.annotation.Annotation;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.SupportedOptions;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.SourceVisitor;

/**
 * A checker for type qualifier systems that only checks subtyping relationships.
 *
 * <p>The annotation(s) are specified on the command line, using an annotation processor argument:
 *
 * <ul>
 *   <li>{@code -Aquals}: specifies the annotations in the qualifier hierarchy (as a comma-separated
 *       list of fully-qualified annotation names with no spaces in between). Only the annotations
 *       for one qualified subtype hierarchy can be passed.
 * </ul>
 *
 * @checker_framework.manual #subtyping-checker Subtying Checker
 */
@SupportedOptions({"quals", "qualDirs"})
public final class SubtypingChecker extends BaseTypeChecker {

  /**
   * Compute SuppressWarnings prefixes, based on the names of all the qualifiers.
   *
   * <p>Provided for the convenience of checkers that do not subclass {@code SubtypingChecker}
   * (because it is final). Clients should call it like:
   *
   * <pre>{@code
   * SubtypingChecker.getSuppressWarningsPrefixes(this.visitor, super.getSuppressWarningsPrefixes());
   * }</pre>
   *
   * @param visitor the visitor
   * @param superSupportedTypeQualifiers the result of super.getSuppressWarningsPrefixes(), as
   *     executed by checker
   * @return SuppressWarnings prefixes, based on the names of all the qualifiers
   */
  public static NavigableSet<String> getSuppressWarningsPrefixes(
      SourceVisitor<?, ?> visitor, NavigableSet<String> superSupportedTypeQualifiers) {
    TreeSet<String> result = new TreeSet<>(superSupportedTypeQualifiers);

    // visitor can be null if there was an error when calling the visitor class's constructor --
    // that is, when there is a bug in a checker implementation.
    if (visitor != null) {
      Set<Class<? extends Annotation>> annos =
          ((BaseTypeVisitor<?>) visitor).getTypeFactory().getSupportedTypeQualifiers();
      for (Class<? extends Annotation> anno : annos) {
        result.add(anno.getSimpleName().toLowerCase());
      }
    }

    return result;
  }

  @Override
  public NavigableSet<String> getSuppressWarningsPrefixes() {
    return getSuppressWarningsPrefixes(this.visitor, super.getSuppressWarningsPrefixes());
  }
}
