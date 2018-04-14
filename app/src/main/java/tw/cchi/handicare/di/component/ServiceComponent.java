package tw.cchi.handicare.di.component;


import dagger.Component;
import tw.cchi.handicare.di.PerService;
import tw.cchi.handicare.di.module.ServiceModule;

@PerService
@Component(dependencies = ApplicationComponent.class, modules = ServiceModule.class)
public interface ServiceComponent {

    // void inject(SyncService service);

}
