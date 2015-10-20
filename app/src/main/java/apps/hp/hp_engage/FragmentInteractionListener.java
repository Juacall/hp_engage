package apps.hp.hp_engage;

/**
 * Created by bernajua on 10/12/2015.
 */
public interface FragmentInteractionListener {

    void closeInfo();
    void openAurasma();
    void showAuraList();
    void showDetails(AuraDetails aura);
    void aurasLoaded(AuraDetails[] auras);

}
