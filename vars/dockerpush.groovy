def call(server, tag) {
    sh """
    docker build -t "${server}/${tag}" .
    docker login -u admin -p admin "${server}"
    docker push "${server}/${tag}"
    docker rmi "${server}/${tag}"
    """
}
