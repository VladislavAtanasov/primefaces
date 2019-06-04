package com.sofia.uni.fmi.web.primefaces.vms.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.opsworks.model.StartInstanceRequest;
import com.sofia.uni.fmi.web.primefaces.ConfigProperties;
import com.sofia.uni.fmi.web.primefaces.mapper.Image;
import com.sofia.uni.fmi.web.primefaces.mapper.ImagesMapper;
import com.sofia.uni.fmi.web.primefaces.views.CreateVmInstance;

@ManagedBean(name = "amazonVirtualMachineService")
@ApplicationScoped
@Component
public class AmazonVirtualMachineService extends SpringBeanAutowiringSupport {

	
	@Autowired
	private ConfigProperties props;
	
	public List<Instance> listVms() {
		AmazonEC2 client = getEc2Client();

		List<Reservation> reservations = client.describeInstances().getReservations();
		if (reservations.isEmpty()) {
			return new ArrayList<>();
		}

		return reservations.stream().flatMap(r -> r.getInstances().stream().filter(
				instance -> !instance.getState().getName().equalsIgnoreCase(InstanceStateName.Terminated.toString())
						&& !instance.getState().getName().equalsIgnoreCase(InstanceStateName.ShuttingDown.toString())))
				.collect(Collectors.toList());
	}

	public String createVm(CreateVmInstance request) {
		String sgName = request.getSgName();
		String keyPairName = request.getKeyPairName();
		String size = request.getSize();
		Image imageName = Image.valueOf(request.getImageName());
		String imageId = new ImagesMapper().getImages().get(imageName);
		if (imageId == null) {
			System.out.println("Cannot find image with name: " + imageName);
			return null;
		}

		AmazonEC2 ec2Client = getEc2Client();
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest().withGroupName(sgName)
				.withDescription(sgName);
		CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);

		IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
		IpPermission ipPermission = new IpPermission().withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
				.withIpProtocol("tcp").withFromPort(80).withToPort(80);

		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest()
				.withGroupName(sgName).withIpPermissions(ipPermission);
		ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName(keyPairName);
		CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(imageId).withInstanceType(size)
				.withKeyName(keyPairName).withMinCount(1).withMaxCount(1).withSecurityGroups(sgName);
		String instanceId = ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0)
				.getInstanceId();

		CreateTagsRequest tagsRequest = new CreateTagsRequest();
		Collection<Tag> tags = new ArrayList<>();
		tags.add(new Tag("imageName", imageName.getName()));
		tagsRequest.setTags(tags);
		tagsRequest.withResources(instanceId);
		ec2Client.createTags(tagsRequest);

		return instanceId;
	}

	public boolean stopVm(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
		StopInstancesResult stopInstancesResult = ec2Client.stopInstances(request);
		return stopInstancesResult.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}

	public boolean startVm(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
		StartInstancesResult sartInstancesResult = ec2Client.startInstances(request);
		return sartInstancesResult.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}

	public boolean terminate(String instanceId) {
		AmazonEC2 ec2Client = getEc2Client();
		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);
		TerminateInstancesResult result = ec2Client.terminateInstances(request);
		return result.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
	}
	
	public void getKeyPairs() {
		AmazonEC2 ec2Client = getEc2Client();

		DescribeKeyPairsResult response = ec2Client.describeKeyPairs();

		for(KeyPairInfo key_pair : response.getKeyPairs()) {
		    System.out.printf(
		        "Found key pair with name %s " +
		        "and fingerprint %s",
		        key_pair.getKeyName(),
		        key_pair.getKeyFingerprint());
		}
	}
	public void createDefaultVm() {
		AmazonEC2 ec2Client = getEc2Client();
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()

				.withGroupName("BaeldungSecurityGroup").withDescription("Baeldung Security Group");
		CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);

		IpRange ipRange = new IpRange().withCidrIp("0.0.0.0/0");
		IpPermission ipPermission = new IpPermission().withIpv4Ranges(Arrays.asList(new IpRange[] { ipRange }))
				.withIpProtocol("tcp").withFromPort(80).withToPort(80);

		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest()
				.withGroupName("BaeldungSecurityGroup").withIpPermissions(ipPermission);
		ec2Client.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);

		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest().withKeyName("baeldung-key-pair");
		CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeyPairRequest);

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId("ami-02f69d40637223896")
				.withInstanceType("t2.micro").withKeyName("baeldung-key-pair").withMinCount(1).withMaxCount(1)
				.withSecurityGroups("BaeldungSecurityGroup");
		String yourInstanceId = ec2Client.runInstances(runInstancesRequest).getReservation().getInstances().get(0)
				.getInstanceId();

		System.out.println("VMS ID " + yourInstanceId);
	}

	private AmazonEC2 getEc2Client() {
		AWSCredentials credentials = new BasicAWSCredentials(props.getConfigValue("api.key"), props.getConfigValue("secret.key"));

		return AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
				.withRegion(Regions.EU_WEST_1).build();
	}

}
