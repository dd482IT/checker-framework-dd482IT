package org.checkerframework.dataflow.cfg.node;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.javacutil.TreeUtils;

/**
 * A node for an assignment:
 *
 * <pre>
 *   <em>variable</em> = <em>expression</em>
 *   <em>expression</em> . <em>field</em> = <em>expression</em>
 *   <em>expression</em> [ <em>index</em> ] = <em>expression</em>
 * </pre>
 *
 * We allow assignments without corresponding AST {@link Tree}s.
 */
public class AssignmentNode extends Node {

  /** The underlying assignment tree. */
  protected final Tree tree;
  /** The node for the LHS of the assignment tree. */
  protected final Node lhs;
  /** The node for the RHS of the assignment tree. */
  protected final Node rhs;

  /**
   * Create a (non-synthetic) AssignmentNode.
   *
   * @param tree the {@code AssignmentTree} corresponding to the {@code AssignmentNode}
   * @param target the lhs of {@code tree}
   * @param expression the rhs of {@code tree}
   */
  public AssignmentNode(Tree tree, Node target, Node expression) {
    super(TreeUtils.typeOf(tree));
    assert tree instanceof AssignmentTree
        || tree instanceof VariableTree
        || tree instanceof CompoundAssignmentTree
        || tree instanceof UnaryTree;
    assert target instanceof FieldAccessNode
        || target instanceof LocalVariableNode
        || target instanceof ArrayAccessNode;
    this.tree = tree;
    this.lhs = target;
    this.rhs = expression;
  }

  /**
   * Returns the left-hand-side of the assignment.
   *
   * @return the left-hand-side of the assignment
   */
  @Pure
  public Node getTarget() {
    return lhs;
  }

  /**
   * Returns the right-hand-side of the assignment.
   *
   * @return the right-hand-side of the assignment
   */
  @Pure
  public Node getExpression() {
    return rhs;
  }

  @Override
  @Pure
  public Tree getTree() {
    return tree;
  }

  @Override
  public <R, P> R accept(NodeVisitor<R, P> visitor, P p) {
    return visitor.visitAssignment(this, p);
  }

  @Override
  @Pure
  public String toString() {
    return getTarget() + " = " + getExpression();
  }

  @Override
  @Pure
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof AssignmentNode)) {
      return false;
    }
    AssignmentNode other = (AssignmentNode) obj;
    return getTarget().equals(other.getTarget()) && getExpression().equals(other.getExpression());
  }

  @Override
  @Pure
  public int hashCode() {
    return Objects.hash(getTarget(), getExpression());
  }

  @Override
  @SideEffectFree
  public Collection<Node> getOperands() {
    return Arrays.asList(getTarget(), getExpression());
  }
}
