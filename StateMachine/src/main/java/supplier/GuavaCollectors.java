//package supplier;
//
//import java.util.EnumSet;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.function.BiConsumer;
//import java.util.function.BinaryOperator;
//import java.util.function.Function;
//import java.util.function.Supplier;
//import java.util.stream.Collector;
//import java.util.stream.Collector.Characteristics;
//
//import com.google.common.collect.ImmutableSet;
//
//public class GuavaCollectors {
//
//  private GuavaCollectors() {
//  }
//
//  
//  public static <T> Collector<T, ?, ImmutableSet<T>> toSet() {
//    Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet.Builder::new;
//    BiConsumer<ImmutableSet.Builder<T>, T> accumulator = ImmutableSet.Builder::add;
//    BinaryOperator<ImmutableSet.Builder<T>> combiner = (left, right) -> { left.addAll(right.build()); return left; };
//    Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;
//    EnumSet<Characteristics> characteristics = EnumSet.of(Characteristics.CONCURRENT, Characteristics.UNORDERED);
//      
//      return new CollectorImpl<T, ImmutableSet.Builder<T>, ImmutableSet<T>>(
//          supplier, 
//          accumulator,
//          combiner,
//          finisher,
//          characteristics);
//  }
//}
