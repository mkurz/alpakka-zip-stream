package controllers;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.alpakka.file.ArchiveMetadata;
import akka.stream.alpakka.file.javadsl.Archive;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import play.libs.ws.WSClient;
import play.mvc.*;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class HomeController extends Controller {

    private final Materializer mat;
    private final WSClient ws;


    @Inject
    public HomeController(Materializer mat, WSClient ws) {
        this.mat = mat;
        this.ws = ws;
    }

    //public CompletionStage<Result> index() throws Exception {
    public Result index() throws Exception {
        final List<String> filesToFetch = new ArrayList<>();
        filesToFetch.add("beef30d054b60379f6d70c29006fc698-grey-ea.svg");
        filesToFetch.add("189558643eb0ada934391a0de7bdccaf-grey-eero.svg");
        filesToFetch.add("62b43200126aca7bf2cfe118e6c96fda-grey-guardian.svg");
        filesToFetch.add("cc444e0e9b2343ef45dd8aa9b366fe52-grey-walmart.svg");
        filesToFetch.add("ba45861ddb9b7fb9874b53003a59ad41-grey-linkedin.svg");

        Source<Pair<ArchiveMetadata, Source<ByteString, NotUsed>>, NotUsed> sources = Source.empty();
        for(String file : filesToFetch) {
            sources = sources.concat(Source.fromCompletionStage(ws.url("https://www.playframework.com/assets/images/home/reference-logos/" + file).stream().thenApply(res ->
                Pair.create(ArchiveMetadata.create("irgendein_subfolder/" + file), (Source<ByteString, NotUsed>)res.getBodyAsSource())
            )));
        }

        // Jetzt datei generieren:
        final Path addedFile = Files.write(Paths.get("hello.txt"), "nur ein test".getBytes(StandardCharsets.UTF_8));

        // Generierte Datei zum zip hinzufügen:
        sources = sources.concat(Source.single(Pair.create(
                ArchiveMetadata.create("anderer_subfolder/" + "generierte_datei.txt"),
                toSource(readFileAsByteString(addedFile))
        )));

        final Source<ByteString, NotUsed> toStream = sources.via(Archive.zip())
                .mapMaterializedValue(cs -> {
                    Files.delete(addedFile); // generierte Datei wieder löschen
                    return cs;
                });

        return ok().chunked(toStream, false, Optional.ofNullable("download.zip"));

        // wenn man die zip stattdessen einfach abspeichern möchte:
/*
        Sink<ByteString, CompletionStage<IOResult>> fileSink = FileIO.toPath(Paths.get("download.zip"));
        CompletionStage<IOResult> ioResult = sources.via(Archive.zip()).runWith(fileSink, mat);
        return ioResult.thenApply(ior -> ok("fertig!"));
*/
    }


    private ByteString readFileAsByteString(Path filePath) throws Exception {
        final Sink<ByteString, CompletionStage<ByteString>> foldSink =
                Sink.fold(ByteString.emptyByteString(), ByteString::concat);

        return FileIO.fromPath(filePath)
                .runWith(foldSink, mat)
                .toCompletableFuture()
                .get(3, TimeUnit.SECONDS);
    }

    private Source<ByteString, NotUsed> toSource(ByteString bs) {
        return Source.single(bs);
    }

}
