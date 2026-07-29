package recruitment.dev.jobofferservice.exception;

public class JobOfferNotFoundException extends RuntimeException {

    public JobOfferNotFoundException(Long jobOfferId) {
        super("Job offer not found with id: " + jobOfferId);
    }
}
