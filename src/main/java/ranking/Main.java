package ranking;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.*;
import java.time.Duration;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        RedBlackTree<String> dateTime = new RedBlackTree<>();
        RedBlackTree<String> timeStamps = new RedBlackTree<>();
        RedBlackTree<Double> A_bids = new RedBlackTree<>();
        RedBlackTree<Double> A_offers = new RedBlackTree<>();
        RedBlackTree<Double> B_bids = new RedBlackTree<>();
        RedBlackTree<Double> B_offers = new RedBlackTree<>();

        String bucketName = "creativewebtech-ranking";
        String objectKey = "data.csv";

        int count = 1;

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        S3Client s3 = S3Client.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();

        // Download object
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        try (InputStream inputStream = s3.getObject(getObjectRequest);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (count == 1) {
                    count++; // skip the headings
                    continue;
                }
                String[] data = line.split(",");
                dateTime.add(data[0]);
                String timeStamp = data[1];
                timeStamps.add(timeStamp);
                Double A_bid = Double.parseDouble(data[2]);
                Double A_offer = Double.parseDouble(data[3]);
                Double B_bid = Double.parseDouble(data[4]);
                Double B_offer = Double.parseDouble(data[5]);
                A_bids.add(A_bid);
                A_offers.add(A_offer);
                B_bids.add(B_bid);
                B_offers.add(B_offer);
            }

            // Analyse the data
            Double biggestA_bid = A_bids.get(A_bids.size() - 1);
            Double smallestA_bid = A_bids.get(0);
            Double medianA_bid = A_bids.get(A_bids.size() / 2);
            Double biggestA_offer = A_offers.get(A_offers.size() - 1);
            Double smallestA_offer = A_offers.get(0);
            Double medianA_offer = A_offers.get(A_offers.size() / 2);

            System.out.printf(
                    "\nBiggest A bid: %.6f\n" + "Smallest A bid: %.6f\n" + "Median A bid: %.6f\n"
                            + "Biggest A offer: %.6f\n" + "Smallest A offer: %.6f\n" + "Median A offer: %.6f\n",
                    biggestA_bid, smallestA_bid, medianA_bid, biggestA_offer, smallestA_offer, medianA_offer);

            //Get the rank of this bid in A_bids
            int rank = A_bids.rank(1.1269261);
            System.out.printf("\n%.7f is the (%dth) in the list of A_bids\n", 1.1269261, rank);

            // B_bids

            Double biggestB_bid = B_bids.get(B_bids.size() - 1);
            Double smallestB_bid = B_bids.get(0);
            Double medianB_bid = B_bids.get(B_bids.size() / 2);
            Double biggestB_offer = B_offers.get(B_offers.size() - 1);
            Double smallestB_offer = B_offers.get(0);
            Double medianB_offer = B_offers.get(B_offers.size() / 2);

            System.out.printf(
                    "\nBiggest B bid: %.6f\n" + "Smallest B bid: %.6f\n" + "Median B bid: %.6f\n"
                            + "Biggest B offer: %.6f\n" + "Smallest B offer: %.6f\n" + "Median B offer: %.6f\n",
                    biggestB_bid, smallestB_bid, medianB_bid, biggestB_offer, smallestB_offer, medianB_offer);
            //Get the rank of this bid in A_bids
            rank = B_bids.rank(1.12692026666667);
            System.out.printf("\n%.7f is the (%dth) in the list of B_bids\n", 1.12692026666667, rank);


        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            s3.close();
        }

        try {

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest
                    .builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(builder -> builder.bucket(bucketName).key(objectKey))
                    .build();

            PresignedGetObjectRequest presignedUrl = presigner.presignGetObject(presignRequest);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            presigner.close();
        }
    }

}