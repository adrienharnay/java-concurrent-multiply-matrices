class SimpleMatrix extends AMatrix {
  private int NB_THREADS_AVAILABLE = Runtime.getRuntime().availableProcessors();

  private AMatrix _m1;
  private AMatrix _m2;
  private int _resultHeight;
  private int _resultWidth;
  private long[] _resultArray;

  SimpleMatrix(int height, int width, long[] array) {
    super(height, width, array);
  }

  class SectionCalculator implements Runnable {
    int _start;
    int _end;

    SectionCalculator(int start, int end) {
      _start = start;
      _end = end;
    }

    public void run () {
      for (int i = _start; i < _end; ++i) {
        for (int k = 0; k < _m1.getWidth(); ++k) {
          for (int j = 0; j < _resultWidth; ++j) {
            _resultArray[i * _resultWidth + j] +=
              _m1.getArray()[i * _m1.getWidth() + k] * _m2.getArray()[k * _m2.getWidth() + j];
          }
        }
      }
    }
  }

  AMatrix add(AMatrix m2) {
    AMatrix m1 = this;
    long resultArray[] = new long[m1.getHeight() * m1.getWidth()];

    for (int i = 0; i < m1.getHeight(); ++i) {
      for (int j = 0; j < m1.getWidth(); ++j) {
        int index = i * m1.getWidth() + j;

        resultArray[index] = m1.getArray()[index] + m2.getArray()[index];
      }
    }

    return new SimpleMatrix(m1.getHeight(), m1.getWidth(), resultArray);
  }

  AMatrix subtract(AMatrix m2) {
    AMatrix m1 = this;
    long resultArray[] = new long[m1.getHeight() * m1.getWidth()];

    for (int i = 0; i < m1.getHeight(); ++i) {
      for (int j = 0; j < m1.getWidth(); ++j) {
        int index = i * m1.getWidth() + j;

        resultArray[index] = m1.getArray()[index] - m2.getArray()[index];
      }
    }

    return new SimpleMatrix(m1.getHeight(), m1.getWidth(), resultArray);
  }

  AMatrix multiplyBy(AMatrix m2) {
    _m1 = this;
    _m2 = m2;
    _resultHeight = _m1.getHeight();
    _resultWidth = _m2.getWidth();
    _resultArray = new long[_resultHeight * _resultWidth];

    int nbThreads = _resultHeight > NB_THREADS_AVAILABLE ? NB_THREADS_AVAILABLE : _resultHeight;
    Thread[] threads = new Thread[nbThreads];
    int sectionSize = _resultHeight / nbThreads;

    for (int i = 0; i < nbThreads; ++i) {
      int start = i * sectionSize;
      int end = (i == nbThreads - 1 ? _resultHeight : (i + 1) * sectionSize);

      threads[i] = new Thread(new SectionCalculator(start, end));
      threads[i].start();
    }

    try {
      for (int i = 0; i < nbThreads; ++i) {
        threads[i].join();
      }
    } catch (InterruptedException e) {
      System.err.println("Thread supposed to compute line has been unexpectedly interrupted");
    }

    return new SimpleMatrix(_resultHeight, _resultWidth, _resultArray);
  }

  AMatrix multiplyBy(AMatrix m2, int availableCores) {
    NB_THREADS_AVAILABLE = availableCores;

    return this.multiplyBy(m2);
  }
}
