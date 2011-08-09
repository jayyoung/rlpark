package rltoys.math.vector.implementations;

import rltoys.math.vector.DenseVector;
import rltoys.math.vector.MutableVector;
import rltoys.math.vector.RealVector;
import rltoys.math.vector.SparseVector;
import rltoys.math.vector.VectorEntry;
import rltoys.utils.NotImplemented;
import rltoys.utils.Utils;

public class Vectors {
  static public boolean equals(RealVector a, RealVector b) {
    if (a == b)
      return true;
    if (a != null && b == null || a == null && b != null)
      return false;
    if (a.getDimension() != b.getDimension())
      return false;
    for (int i = 0; i < a.getDimension(); ++i)
      if (a.getEntry(i) != b.getEntry(i))
        return false;
    return true;
  }

  public static boolean checkValues(RealVector v) {
    for (VectorEntry entry : v)
      if (!Utils.checkValue(entry.value()))
        return false;
    return true;
  }

  public static void clear(MutableVector vector) {
    if (vector instanceof DenseVector) {
      ((DenseVector) vector).set(0.0);
      return;
    }
    if (vector instanceof SparseVector) {
      ((SparseVector) vector).clear();
      return;
    }
    throw new NotImplemented();
  }

  static public MutableVector absToSelf(MutableVector v) {
    for (VectorEntry entry : v)
      v.setEntry(entry.index(), Math.abs(entry.value()));
    return v;
  }

  static public double sum(RealVector v) {
    double sum = 0.0;
    for (VectorEntry entry : v)
      sum += entry.value();
    return sum;
  }
}
