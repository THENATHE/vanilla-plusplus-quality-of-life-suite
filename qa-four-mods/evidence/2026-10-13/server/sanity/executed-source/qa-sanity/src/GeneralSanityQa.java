package qa;
import java.nio.file.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
public final class GeneralSanityQa implements ModInitializer {
 public void onInitialize(){ServerLifecycleEvents.SERVER_STARTED.register(server->{try{
  // Synthetic fixture has no connected player to activate chunks; expose its loaded chunks to normal entity queries.
  var level=server.overworld(); var field=net.minecraft.server.level.ServerLevel.class.getDeclaredField("entityManager"); field.setAccessible(true);
  var manager=(net.minecraft.world.level.entity.PersistentEntitySectionManager<net.minecraft.world.entity.Entity>)field.get(level);
  for(int x=-1;x<4;x++)for(int z=-1;z<4;z++){level.getChunk(x,z);manager.updateChunkStatus(new net.minecraft.world.level.ChunkPos(x,z),net.minecraft.world.level.entity.Visibility.TRACKED);}
  int ssoCount=sso.qa.RepairChecks.run(server);int storage=StorageChecks.run(server);int chalkCount=chalk.qa.ChalkChecks.run(server);
  Files.writeString(Path.of("suite-mechanics-result.txt"),"PASS "+ssoCount+" broader SSO, "+storage+" storage, "+chalkCount+" chalk placement assertions\n");
 }catch(Throwable t){t.printStackTrace();try{Files.writeString(Path.of("suite-mechanics-result.txt"),"FAIL "+t);}catch(Exception ignored){}}finally{server.halt(false);}});}
}
