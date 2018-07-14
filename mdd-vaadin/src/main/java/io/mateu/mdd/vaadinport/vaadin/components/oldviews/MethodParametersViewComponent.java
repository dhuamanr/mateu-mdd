package io.mateu.mdd.vaadinport.vaadin.components.oldviews;

import com.vaadin.data.HasValue;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.ui.ErrorLevel;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import io.mateu.mdd.core.MDD;
import io.mateu.mdd.core.annotations.Action;
import io.mateu.mdd.core.annotations.Ignored;
import io.mateu.mdd.core.data.Data;
import io.mateu.mdd.core.interfaces.AbstractStylist;
import io.mateu.mdd.core.reflection.FieldInterfaced;
import io.mateu.mdd.core.reflection.ReflectionHelper;
import io.mateu.mdd.core.util.Helper;
import io.mateu.mdd.vaadinport.vaadin.MyUI;
import io.mateu.mdd.vaadinport.vaadin.data.MDDBinder;
import javafx.util.Pair;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodParametersViewComponent extends AbstractViewComponent {

    private final Method method;
    private final Object bean;
    private Map<HasValue, List<Validator>> validators = new HashMap<>();

    private Map<String, Object> model = new HashMap<>();

    private MDDBinder binder;

    private AbstractStylist stylist;

    public List<Object> getMergeables() {
        return binder.getMergeables();
    }

    public MethodParametersViewComponent(Object bean, Method method) {

        this.bean = bean;
        this.method = method;

        String t = Helper.capitalize(method.getName());
        if (method.isAnnotationPresent(Action.class)) t = method.getAnnotation(Action.class).name();
        setViewTitle(t);
        //setViewTitle(stylist.getViewTitle(newRecord, model));

        try {
            build();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MethodParametersViewComponent build() throws IllegalAccessException, InstantiationException {

        super.build();

        addStyleName("editorviewcomponent");


        binder = new MDDBinder(model.getClass());
        //binder = new Binder(modelType, true);

        Pair<Component, AbstractStylist> r = FormLayoutBuilder.build(binder, model.getClass(), model, validators, getAllFields());

        stylist = r.getValue();
        addComponent(r.getKey());

        return this;
    }

    private List<FieldInterfaced> getAllFields() {
        List<FieldInterfaced> allFields = ReflectionHelper.getAllFields(method);

        allFields = allFields.stream().filter((f) ->
                !(f.isAnnotationPresent(Ignored.class) || (f.isAnnotationPresent(Id.class) && f.isAnnotationPresent(GeneratedValue.class)))
        ).collect(Collectors.toList());

        return allFields;
    }

    @Override
    public void addViewActionsMenuItems(MenuBar bar) {

        super.addViewActionsMenuItems(bar);

        bar.addItem("Run", VaadinIcons.BOLT, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem menuItem) {
                try {

                    //binder.writeBean(model);

                    ValueContext vc = new ValueContext();
                    for (HasValue h : validators.keySet()) for (Validator v : validators.get(h)) {
                        ValidationResult r = v.apply(h.getValue(), vc);
                        if (h instanceof AbstractComponent) {
                            AbstractComponent c = (AbstractComponent) h;
                            if (r.isError()) {
                                c.setComponentError(new ErrorMessage() {
                                    @Override
                                    public ErrorLevel getErrorLevel() {
                                        return r.getErrorLevel().get();
                                    }

                                    @Override
                                    public String getFormattedHtmlMessage() {
                                        return r.getErrorMessage();
                                    }
                                });
                            } else {
                                c.setComponentError(null);
                            }
                        }
                    }

                    if (binder.allValid()) {


                        System.out.println(binder.getBean());


                        Object r = ReflectionHelper.execute(MDD.getUserData(), method, new Data(binder.getBean()), bean);

                        if (void.class.equals(method.getReturnType())) {
                            MyUI.get().getNavegador().goBack();
                            MDD.alert("Done");
                        } else {
                            MyUI.get().getNavegador().showResult(method, r);
                        }

                        // cambiamos la url, para reflejar el cambio



                    }
                    else Notification.show("There are errors", Notification.Type.ERROR_MESSAGE);


                } catch (Throwable throwable) {
                    MDD.alert(throwable);
                }
            }
        });
    }

    public AbstractStylist getStylist() {
        return stylist;
    }

    public void setStylist(AbstractStylist stylist) {
        this.stylist = stylist;
    }


}