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

        final Source<ByteString, NotUsed> toStream = sources.via(Archive.zip());
        // Hier könnte man dann etwas machen wenn das ganze erledig ist, z.b. eben Datei löschen:
//                .mapMaterializedValue(cs -> {
//                    Files.delete(Paths.get("/die/datei/.pdf"));
//                    return cs;
//                });

        return ok().chunked(toStream, false, Optional.ofNullable("download.zip"));

        // wenn man die zip stattdessen einfach abspeichern möchte:
/*
        Sink<ByteString, CompletionStage<IOResult>> fileSink = FileIO.toPath(Paths.get("download.zip"));
        CompletionStage<IOResult> ioResult = sources.via(Archive.zip()).runWith(fileSink, mat);
        return ioResult.thenApply(ior -> ok("fertig!"));
*/
    }

}
