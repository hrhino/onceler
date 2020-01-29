package onceler.internal;

import java.lang.invoke.*;

public class Factory {
  private Factory() {}

  public static CallSite bootstrap(
    MethodHandles.Lookup lookup
  , String invokedName
  , MethodType invokedType
  , MethodHandle factory
  ) throws Throwable {
    return new ConstantCallSite(MethodHandles.constant(invokedType.returnType(), factory.invoke()));
  }
}
     
