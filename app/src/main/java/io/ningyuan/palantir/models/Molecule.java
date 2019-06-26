package io.ningyuan.palantir.models;

import android.net.Uri;

import java.io.File;

import io.ningyuan.jPdbApi.Pdb;
import io.ningyuan.palantir.utils.FileIo;

public class Molecule {
    private File glbFile;
    private Pdb pdb;
    private Uri pdbFileUri;

    public void setGlbFile(File glbFile) {
        this.glbFile = glbFile;
    }

    public void setPdb(Pdb pdb) {
        this.pdb = pdb;
    }

    public void setPdbFileUri(Uri pdbFileUri) {
        this.pdbFileUri = pdbFileUri;
    }

    public void setPdbFileUri(java.net.URI pdbFileUri) {
        this.pdbFileUri = FileIo.javaUriToAndroidUri(pdbFileUri);
    }

    public File getGlbFile() {
        return glbFile;
    }

    public Pdb getPdb() {
        return pdb;
    }

    public Uri getPdbFileUri() {
        return pdbFileUri;
    }
}
