package pl.karoldronia.artefacts.config.impl;

import com.eternalcode.multification.notice.Notice;
import eu.okaeri.configs.OkaeriConfig;

public class MessageConfig extends OkaeriConfig {

    public Notice invalidUsage = Notice.chat("&4Wrong command usage &8>> &7{COMMAND}.");

    public Notice invalidUsageHeader = Notice.chat("&cWrong command usage!");

    public Notice invalidUsageEntry = Notice.chat("&8 >> &7{SCHEME}");

    public Notice noPermission = Notice.chat("&4You do not have permission to use this command!");

    public Notice cantFindPlayer = Notice.chat("&4Can not find that player!");

    public Notice onlyForPlayer = Notice.chat("&4Command only for players!");

    public Notice oceanAbility = Notice.actionbar(
            "<gradient:#ffffff:#0029ff>You have used ocean ability affecting {affected}!</gradient>"
    );

    public Notice thunderAbility = Notice.actionbar(
            "&eYou have used thunder ability affecting &9{affected}&e!"
    );

    public Notice fireAbility = Notice.actionbar(
            "&eYou have used fire ability affecting &9{affected}&e!"
    );

    public Notice upgradedFireAbility = Notice.actionbar(
            "&eYou have used upgraded fire ability!"
    );

    public Notice earthAbility = Notice.actionbar(
            "&eYou have used earth ability affecting &9{affected}&e!"
    );

    public Notice earthUpgradedAbility = Notice.actionbar(
            "<gradient:#4affff:#4aff00>You have used upgraded earth ability affecting {affected}!</gradient>"
    );

    public Notice playerDashedThrough = Notice.chat(
            "&ePlayer &9{player} &edashed through you!"
    );

    public Notice airAbility = Notice.actionbar(
            "<gradient:#ffffff:#00ffff>You dash forward {distance} blocks and hitting {targets} players!</gradient>"
    );

    public Notice noTargetsForUpgradedAirAbility = Notice.chat(
            "&cNo targets found for advanced dash!"
    );

    public Notice airAbilitySlamDown = Notice.chat(
            "&9{player} &eslams down with tremendous force!"
    );

    public Notice iceAbility = Notice.actionbar(
            "<gradient:#ffffff:#00ffff>You have used ice ability affecting {affected}!</gradient>"
    );

    public Notice iceUpgradedAbility = Notice.actionbar(
            "<gradient:#ffffff:#00ffff>You have used upgraded ice ability affecting {affected}!</gradient>"
    );

    public Notice sculkAbility = Notice.actionbar(
            "<gradient:#ffffff:#3c0065>You have used sculk ability affecting {affected}!</gradient>"
    );

    public Notice sculkDomainCreated = Notice.actionbar(
            "<gradient:#ffffff:#3c0065>You have created a sculk domain!</gradient>"
    );

    public Notice lifeAbility = Notice.actionbar(
            "<gradient:#ffffff:#d33c57>You have used life ability healing yourself!</gradient>"
    );

    public Notice lifeUpgradedAbility = Notice.actionbar(
            "<gradient:#ffffff:#d33c57>You have used life ability affecting {affected}!</gradient>"
    );

    public Notice strengthAbility = Notice.actionbar(
            "<gradient:#ffffff:#380080>You have used stength abiltiy!</gradient>"
    );

    public Notice upgradedStrengthAbility = Notice.actionbar(
            "<gradient:#ffffff:#380080>You have used upgraded strength ability!</gradient>"
    );

    public Notice luckAbility = Notice.actionbar(
            "<gradient:#ffffff:#00ff00>You have used luck ability giving you {effect} effect!</gradient>"
    );

    public Notice upgradedLuckAbility = Notice.actionbar(
            "<gradient:#ffffff:#00ff00>You have used upgraded luck ability affecting {affected}!</gradient>"
    );

    public Notice artefactNotFound = Notice.chat(
            "&cCannot find artefact with this id!"
    );

    public Notice artefactSet = Notice.chat(
            "&aYou have set &e{id} artefact &ato player &e{player}&a!"
    );

    public Notice upgradedRequired = Notice.chat(
            "&cThis ability requires an upgraded artefact to be used!"
    );

    public Notice traderItemUsed = Notice.chat(
            "&aYou have used a trader item, you have received &e{artefact} artefact!"
    );

}