package io.mateu.ui.mdd.client;

import io.mateu.ui.core.client.app.AbstractAction;
import io.mateu.ui.core.client.views.AbstractView;
import io.mateu.ui.core.shared.Data;
import io.mateu.ui.mdd.server.ERPServiceImpl;
import io.mateu.ui.mdd.server.util.Helper;

import java.util.ArrayList;
import java.util.List;

public class PMOView extends AbstractView {

    private final Class pmoClass;
    private Data metadata;

    @Override
    public Data initializeData() {
        Data data = new Data();
        try {
            ERPServiceImpl.fillData(null, null, data, pmoClass.newInstance(), null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return data;
    }

    @Override
    public String getViewIdBase() {
        return "pmo/" + pmoClass.getName();
    }

    @Override
    public String getViewId() {
        return getViewIdBase();
    }

    public PMOView(Class pmoClass) {
        this.pmoClass = pmoClass;

        try {
            metadata = new ERPServiceImpl().getMetaData(null, pmoClass.getName(), pmoClass.getName(), null).getData("_editorform");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getTitle() {
        return Helper.capitalize(pmoClass.getSimpleName());
    }

    @Override
    public List<AbstractAction> getActions() {
        List<AbstractAction> as = super.createActions();
        for (Data da : metadata.getList("_actions")) if (da.isEmpty("_addasbutton")) {
            as.add(MDDJPACRUDView.createAction(this, da));
        }
        return as;
    }

    @Override
    public void build() {
        MDDJPACRUDView.buildFromMetadata(this, metadata, false);
    }
}
