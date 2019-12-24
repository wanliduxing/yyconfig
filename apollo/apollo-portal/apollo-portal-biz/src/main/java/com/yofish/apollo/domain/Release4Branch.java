package com.yofish.apollo.domain;

import com.yofish.apollo.strategy.PublishStrategy4Branch;
import com.yofish.apollo.strategy.PublishStrategy4Main;
import common.constants.GsonType;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.*;

import static com.yofish.apollo.strategy.CalculateUtil.mergeConfiguration;
import static com.yofish.gary.bean.StrategyNumBean.getBeanByClass;

/**
 * @Author: xiongchengwei
 * @Date: 2019/12/17 上午10:54
 */
@Data
@Entity
@DiscriminatorValue("Release4Branch")
public class Release4Branch extends Release {

    @Builder
    public Release4Branch(AppEnvClusterNamespace namespace, String name, String comment, Map<String, String> configurations, boolean isEmergencyPublish, String releaseKey) {
        super(namespace, name, comment, configurations, isEmergencyPublish, releaseKey);
    }

    public Release4Branch() {

    }


    @Override
    public Release publish() {

        return getBeanByClass(PublishStrategy4Branch.class).publish(this);
    }


    public Release4Main getMainRelease() {
        return null;
    }

    public void rollback(Release4Main release4Main, List<Release> twoLatestActiveReleases) {


        Release4Branch newRelease4Branch = createNewBranchNamespace(this, twoLatestActiveReleases);

        newRelease4Branch.publish();

        //        branchRelease(parentNamespace, childNamespace, TIMESTAMP_FORMAT.format(new Date()) + "-master-rollback-merge-to-gray", "",
//                childNamespaceNewConfiguration, parentNamespaceNewLatestRelease.getId(), operator,
//                ReleaseOperation.MATER_ROLLBACK_MERGE_TO_GRAY, false);

    }

    public Release4Branch createNewBranchNamespace(Release release, List<Release> parentNamespaceTwoLatestActiveRelease) {
        FastDateFormat TIMESTAMP_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");

        Release4Branch release4Branch = new Release4Branch();

        Release parentNamespaceNewLatestRelease = parentNamespaceTwoLatestActiveRelease.get(1);

        Release abandonedRelease = parentNamespaceTwoLatestActiveRelease.get(0);
        Map<String, String> parentNamespaceAbandonedConfiguration = gson.fromJson(abandonedRelease.getConfigurations(), GsonType.CONFIG);

        Map<String, String> parentNamespaceNewLatestConfiguration = gson.fromJson(parentNamespaceNewLatestRelease.getConfigurations(), GsonType.CONFIG);

        Map<String, String> childNamespaceNewConfiguration = calculateBranchConfigToPublish(parentNamespaceAbandonedConfiguration, parentNamespaceNewLatestConfiguration, release.getAppEnvClusterNamespace());

//        release4Branch.setName( TIMESTAMP_FORMAT.format(new Date()) + "-master-rollback-merge-to-gray", "");

        return release4Branch;
    }


    private Map<String, String> calculateBranchConfigToPublish(Map<String, String> mainNamespaceOldConfiguration, Map<String, String> mainNamespaceNewConfiguration,
                                                               AppEnvClusterNamespace branchNamespace) {
        //first. calculate child appNamespace modified configs
        Release childNamespaceLatestActiveRelease = branchNamespace.findLatestActiveRelease();

        Map<String, String> childNamespaceLatestActiveConfiguration = childNamespaceLatestActiveRelease == null ? null : gson.fromJson(childNamespaceLatestActiveRelease.getConfigurations(), GsonType.CONFIG);

        Map<String, String> childNamespaceModifiedConfiguration = calculateBranchModifiedItemsAccordingToRelease(mainNamespaceOldConfiguration, childNamespaceLatestActiveConfiguration);

        //second. append child appNamespace modified configs to parent appNamespace new latest configuration
        return mergeConfiguration(mainNamespaceNewConfiguration, childNamespaceModifiedConfiguration);
    }

    private Map<String, String> calculateBranchModifiedItemsAccordingToRelease(
            Map<String, String> masterReleaseConfigs,
            Map<String, String> branchReleaseConfigs) {

        Map<String, String> modifiedConfigs = new HashMap<>();

        if (CollectionUtils.isEmpty(branchReleaseConfigs)) {
            return modifiedConfigs;
        }

        if (CollectionUtils.isEmpty(masterReleaseConfigs)) {
            return branchReleaseConfigs;
        }

        for (Map.Entry<String, String> entry : branchReleaseConfigs.entrySet()) {

            if (!Objects.equals(entry.getValue(), masterReleaseConfigs.get(entry.getKey()))) {
                modifiedConfigs.put(entry.getKey(), entry.getValue());
            }
        }

        return modifiedConfigs;

    }


}
