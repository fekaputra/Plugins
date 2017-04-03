package org.dbpedia.extraction.spark.plus;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Created by chile on 22.03.17.
 * Creates a Stream of Statements based on the StatementSupplier
 * This Class should be used when reading from a RepositoryConnection.
 * When  reading (RDF) files we will use the internal SPARK methods of distributing file segments to the worker nodes
 */
public class StatementStream implements Stream<Statement> {

    private Stream<Statement> stream;
    private StatementSupplier handler = new StatementSupplier();



/*    public StatementStream(FilesDataUnit files) throws IOException, RDFParseException, RDFHandlerException {
        Stream<Statement> streamInit = Stream.empty();
        try {
            for (FilesDataUnit.Entry entry : FilesHelper.getFiles(files)) {
                RDFFormat format = Rio.getParserFormatForFileName(entry.getFileURIString(), null);
                RDFParser rdfParser = Rio.createParser(format);
                rdfParser.setRDFHandler(handler);
            }
        } catch (DataUnitException e) {
            throw new IOException(e);
        }
        stream = Stream.generate(handler);
    }*/

    public StatementStream(RepositoryConnection connection, Resource... graphs) throws IOException, RDFParseException, RDFHandlerException, RepositoryException {
        stream = Stream.generate(handler);
        connection.export(handler, graphs);
    }

    @Override
    public Stream<Statement> filter(Predicate<? super Statement> predicate) {
        return stream.filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super Statement, ? extends R> function) {
        return stream.map(function);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super Statement> toIntFunction) {
        return stream.mapToInt(toIntFunction);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super Statement> toLongFunction) {
        return stream.mapToLong(toLongFunction);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super Statement> toDoubleFunction) {
        return stream.mapToDouble(toDoubleFunction);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super Statement, ? extends Stream<? extends R>> function) {
        return stream.flatMap(function);
    }

    @Override
    public IntStream flatMapToInt(Function<? super Statement, ? extends IntStream> function) {
        return stream.flatMapToInt(function);
    }

    @Override
    public LongStream flatMapToLong(Function<? super Statement, ? extends LongStream> function) {
        return stream.flatMapToLong(function);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super Statement, ? extends DoubleStream> function) {
        return stream.flatMapToDouble(function);
    }

    @Override
    public Stream<Statement> distinct() {
        return stream.distinct();
    }

    @Override
    public Stream<Statement> sorted() {
        return stream.sorted();
    }

    @Override
    public Stream<Statement> sorted(Comparator<? super Statement> comparator) {
        return stream.sorted(comparator);
    }

    @Override
    public Stream<Statement> peek(Consumer<? super Statement> consumer) {
        return stream.peek(consumer);
    }

    @Override
    public Stream<Statement> limit(long l) {
        return stream.limit(l);
    }

    @Override
    public Stream<Statement> skip(long l) {
        return stream.skip(l);
    }

    @Override
    public void forEach(Consumer<? super Statement> consumer) {
        stream.forEach(consumer);
    }

    @Override
    public void forEachOrdered(Consumer<? super Statement> consumer) {
        stream.forEachOrdered(consumer);
    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> intFunction) {
        return stream.toArray(intFunction);
    }

    @Override
    public Statement reduce(Statement statement, BinaryOperator<Statement> binaryOperator) {
        return stream.reduce(binaryOperator).orElse(null);
    }

    @Override
    public Optional<Statement> reduce(BinaryOperator<Statement> binaryOperator) {
        return stream.reduce(binaryOperator);
    }

    @Override
    public <U> U reduce(U u, BiFunction<U, ? super Statement, U> biFunction, BinaryOperator<U> binaryOperator) {
        return stream.reduce(u, biFunction, binaryOperator);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Statement> biConsumer, BiConsumer<R, R> biConsumer1) {
        return stream.collect(supplier, biConsumer, biConsumer1);
    }

    @Override
    public <R, A> R collect(Collector<? super Statement, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<Statement> min(Comparator<? super Statement> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<Statement> max(Comparator<? super Statement> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super Statement> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super Statement> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super Statement> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<Statement> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<Statement> findAny() {
        return stream.findAny();
    }

    @Override
    public Iterator<Statement> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<Statement> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public Stream<Statement> sequential() {
        return stream.sequential();
    }

    @Override
    public Stream<Statement> parallel() {
        return stream.parallel();
    }

    @Override
    public Stream<Statement> unordered() {
        return stream.unordered();
    }

    @Override
    public Stream<Statement> onClose(Runnable runnable) {
        return stream.onClose(runnable);
    }

    @Override
    public void close() {
        stream.close();
    }
}
