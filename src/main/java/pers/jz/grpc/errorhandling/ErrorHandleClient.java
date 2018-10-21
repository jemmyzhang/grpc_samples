package pers.jz.grpc.errorhandling;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pers.jz.grpc.helloworld.GreeterGrpc;
import pers.jz.grpc.helloworld.HelloReply;
import pers.jz.grpc.helloworld.HelloRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jemmy Zhang on 2018/10/16.
 */
public class ErrorHandleClient {

    private static final Logger logger = Logger.getLogger(ErrorHandleClient.class.getName());
    private ManagedChannel channel;
    ErrorServiceGrpc.ErrorServiceBlockingStub blockingStub;

    public ErrorHandleClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    private ErrorHandleClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = ErrorServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void callCustomUnwrapException(EchoRequest request) {
        try {
            blockingStub.customUnwrapException(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNKNOWN) {
                logger.log(Level.SEVERE, "Server threw another exception... Not sure which one!", e);
            }
        }
    }

    public void callCustomException(EchoRequest request) {
        try {
            blockingStub.customException(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.INTERNAL) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void callUncaughtExceptions(EchoRequest request) {
        try {
            blockingStub.uncaughtExceptions(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNKNOWN) {
                logger.log(Level.SEVERE, "Server threw an exception... Not sure which one!", e);
            }
        }
    }

    public void callDeadlineExceededException(EchoRequest request) {
        try {
            blockingStub.withDeadlineAfter(2, TimeUnit.SECONDS).deadlineExceeded(request);
        } catch (StatusRuntimeException e) {
            // Do not use Status.equals(...) - it's not well defined. Compare Code directly.
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                logger.log(Level.SEVERE, "Deadline exceeded!", e);
            }
        }
    }

    public void callAutomaticallyWrappedException(EchoRequest request) {
        try {
            blockingStub.automaticallyWrappedException(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void callNotImplemented(EchoRequest request) {
        try {
            blockingStub.notImplemented(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNIMPLEMENTED) {
                logger.log(Level.SEVERE, "Operation not implemented", e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ErrorHandleClient errorHandleClient = new ErrorHandleClient("127.0.0.1", 8080);
        EchoRequest request = EchoRequest.newBuilder().build();
        errorHandleClient.callCustomUnwrapException(request);
        errorHandleClient.callCustomException(request);
        errorHandleClient.callUncaughtExceptions(request);
        errorHandleClient.callDeadlineExceededException(request);
        errorHandleClient.callAutomaticallyWrappedException(request);
        errorHandleClient.callNotImplemented(request);
        errorHandleClient.shutdown();
    }
}
