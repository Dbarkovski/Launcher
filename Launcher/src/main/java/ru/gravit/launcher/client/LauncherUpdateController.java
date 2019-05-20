package ru.gravit.launcher.client;

import ru.gravit.launcher.NewLauncherSettings;
import ru.gravit.launcher.downloader.ListDownloader;
import ru.gravit.launcher.events.request.UpdateRequestEvent;
import ru.gravit.launcher.hasher.HashedDir;
import ru.gravit.launcher.hasher.HashedEntry;
import ru.gravit.launcher.hasher.HashedFile;
import ru.gravit.launcher.managers.SettingsManager;
import ru.gravit.launcher.request.update.UpdateRequest;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.LogHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LauncherUpdateController implements UpdateRequest.UpdateController {
    @Override
    public void preUpdate(UpdateRequest request, UpdateRequestEvent e) {

    }

    @Override
    public void preDiff(UpdateRequest request, UpdateRequestEvent e) {

    }

    @Override
    public void postDiff(UpdateRequest request, UpdateRequestEvent e, HashedDir.Diff diff) throws IOException {
        if(e.zip) return;
        if(SettingsManager.settings.featureStore)
        {
            LogHelper.info("Enabled HStore feature. Find");
            AtomicReference<NewLauncherSettings.HashedStoreEntry> lastEn = null;
            ArrayList<String> removed = new ArrayList<>();
            diff.mismatch.walk(File.separator, (path, name, entry) -> {
                if(entry.getType() == HashedEntry.Type.DIR) return false;
                HashedFile file = (HashedFile) entry;
                //Первый экспериментальный способ - честно обходим все возможные Store
                Path ret = null;
                if(lastEn.get() == null)
                {
                    for(NewLauncherSettings.HashedStoreEntry en : SettingsManager.settings.lastHDirs)
                    {
                        ret = tryFind(en, file);
                        if(ret != null) {
                            lastEn.set(en);
                            break;
                        }
                    }
                }
                else {
                    ret = tryFind(lastEn.get(), file);
                }
                if(ret == null)
                {
                    for(NewLauncherSettings.HashedStoreEntry en : SettingsManager.settings.lastHDirs)
                    {
                        ret = tryFind(en, file);
                        if(ret != null) {
                            lastEn.set(en);
                            break;
                        }
                    }
                }
                if(ret != null)
                {
                    //Еще раз проверим корректность хеша
                    //Возможно эта проверка избыточна
                    if(file.isSame(ret, true))
                    {
                        Path source = request.getDir().resolve(path).resolve(name);
                        LogHelper.debug("Copy file %s to %s", source.toAbsolutePath().toString(), ret.toAbsolutePath().toString());
                        //Let's go!
                        Files.copy(ret, source);
                        removed.add(path.concat(File.separator).concat(name));
                    }
                }
                return false;
            });
            for(String rem : removed)
            {
                diff.mismatch.removeR(rem);
            }
        }
    }
    public Path tryFind(NewLauncherSettings.HashedStoreEntry en, HashedFile file) throws IOException
    {
        AtomicReference<Path> ret = null;
        en.hdir.walk(File.separator, (path, name, entry) -> {
            if(entry.getType() == HashedEntry.Type.DIR) return false;
            HashedFile tfile = (HashedFile) entry;
            if(tfile.isSame(file))
            {
                LogHelper.debug("[DIR:%s] Found file %s in %s", en.name, name, path);
                Path tdir = Paths.get(en.fullPath).resolve(path).resolve(name);
                try {
                    if(tfile.isSame(tdir, true))
                    {
                        LogHelper.debug("[DIR:%s] Confirmed file %s in %s", en.name, name, path);
                        ret.set(tdir);
                        return true;
                    }
                } catch (IOException e)
                {
                    LogHelper.error("Check file error %s", e.getMessage());
                }
            }
            return false;
        });
        return ret.get();
    }

    @Override
    public void preDownload(UpdateRequest request, UpdateRequestEvent e, List<ListDownloader.DownloadTask> adds) {

    }

    @Override
    public void postDownload(UpdateRequest request, UpdateRequestEvent e) {

    }

    @Override
    public void postUpdate(UpdateRequest request, UpdateRequestEvent e) {

    }
}